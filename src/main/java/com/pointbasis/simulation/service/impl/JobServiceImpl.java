package com.pointbasis.simulation.service.impl;

import static com.pointbasis.simulation.others.Constants.CLUSTERKEY;
import static com.pointbasis.simulation.others.Constants.S3_BUCKET_LINK;

import com.pointbasis.simulation.domain.*;
import com.pointbasis.simulation.service.dto.GeometryDTO;
import com.pointbasis.simulation.repository.*;
import com.pointbasis.simulation.service.JobService;
import com.pointbasis.simulation.service.Services;
import com.pointbasis.simulation.service.UserService;
import java.util.*;
import org.bson.types.ObjectId;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

@Service
public class JobServiceImpl implements JobService {

    private final Logger log = LoggerFactory.getLogger(JobServiceImpl.class);

    @Autowired
    private Services services;

    @Autowired
    private JobRepository jobRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private UserService userService;

    @Autowired
    private ProjectRepository projectRepository;

    @Autowired
    private GeometryRepository modelRepository;

    @Autowired
    private MeshRepository meshRepository;

    @Autowired
    private MeshServiceImpl meshService;

    @Autowired
    private AccountsRepository accountingRepository;

    @Override
    public String getJobStatus(String jobId, String ip) {
        String status = "";
        try {
            String command = "ssh -i " + CLUSTERKEY + " " + ip + " squeue -j " + jobId;
            System.out.println(command);
            Map<String, String> output = services.run(command);
            status = output.get("successfulOutput").trim().replaceAll("\\s{1,}", "-").split("-")[13];
        } catch (Exception e) {
            System.out.println(e);
        }
        System.out.println(status);
        return status;
    }

    @Override
    public ResponseEntity cancelJob(String jobId) {
        try {
            String ip = services.getIP();
            String command = "ssh -i " + CLUSTERKEY + " " + ip + " scancel " + jobId;
            services.run(command);
            System.out.println("Terminated the job");
            return deleteJob(jobId, ip);
        } catch (Exception e) {
            System.out.println(e);
            return ResponseEntity.badRequest().build();
        }
    }

    @Override
    public ResponseEntity deleteJob(String jobId, String ip) throws Exception {
        Optional<Job> job = jobRepository.findByJobId(jobId);
        if (job.isPresent()) {
            if (job.get().getMesh() != null) meshService.deleteMesh(job.get().getMesh().getId(), ip);
            jobRepository.delete(job.get());
            return ResponseEntity.ok().build();
        }
        return ResponseEntity.badRequest().build();
    }

    @Override
    @Scheduled(fixedRate = 60000)
    public void updateJobStatus() {
        List<Job> jobs = jobRepository.findAll();
        try {
            String ip = services.getIP();
            String status;
            String command;
            Map<String, String> output;
            for (Job job : jobs) {
                String jobId = job.getJobId();
                command = "ssh -i " + CLUSTERKEY + " " + ip + " squeue -j " + jobId;
                System.out.println(command);
                try {
                    output = services.run(command);
                    status = output.get("successfulOutput").trim().replaceAll("\\s+", "-").split("-")[13];
                    System.out.println(jobId + " --> " + status);
                    job.setJobStatus(status);
                    jobRepository.save(job);
                } catch (Exception e) {
                    String userId = job.getUser().getId();
                    String projectId = job.getProject().getId();
                    String modelId = job.getMesh().getModel().getId();
                    command =
                        "ssh -i " +
                        CLUSTERKEY +
                        " " +
                        ip +
                        " test -f /shared/" +
                        userId +
                        "/" +
                        projectId +
                        "/" +
                        modelId +
                        "/outputFiles/" +
                        job.getMesh().getModel().getName().split("\\.")[0] +
                        ".msh" +
                        ";echo $?";
                    String out = (services.run(command)).get("successfulOutput");
                    System.out.println(out);
                    if (out.equals("0")) {
                        Thread thread = new Thread(() -> completeJob(job, ip));
                        thread.setDaemon(true);
                        thread.start();
                    } else {
                        job.setJobStatus("Failed");
                        job.setElapsedTime("-1");
                        job.setnCPUs(0);
                        Mesh mesh = job.getMesh();
                        meshRepository.delete(mesh);
                        jobRepository.save(job);
                    }
                }
            }
        } catch (Exception e) {
            System.out.println(e);
        }
    }

    @Override
    public void completeJob(Job job, String ip) {
        System.out.println("completing job");
        Map<String, String> jobCompletionDetails;
        String userId = job.getUser().getId();
        String projectId = job.getProject().getId();
        String modelId = job.getMesh().getModel().getId();
        Map<String, String> outputLink = job.getMesh().getOutputLink();
        StringBuilder content = new StringBuilder();
        content.append("ssh -i " + CLUSTERKEY + " " + ip + " ");
        content
            .append("sudo aws s3 cp /shared/")
            .append(userId)
            .append("/")
            .append(projectId)
            .append("/")
            .append(modelId)
            .append("/outputFiles/")
            .append(job.getMesh().getModel().getName().split("\\.")[0])
            .append(".msh")
            .append(" ")
            .append(S3_BUCKET_LINK)
            .append(outputLink.get("msh"));
        try {
            services.run(content.toString());
            content.setLength(0);
            content.append("ssh -i " + CLUSTERKEY + " " + ip + " ");
            content
                .append("sudo aws s3 cp /shared/")
                .append(userId)
                .append("/")
                .append(projectId)
                .append("/")
                .append(modelId)
                .append("/outputFiles/")
                .append(job.getMesh().getModel().getName().split("\\.")[0])
                .append(".msh__sf.obj")
                .append(" ")
                .append(S3_BUCKET_LINK)
                .append(outputLink.get("obj"));
            services.run(content.toString());

            content.setLength(0);
            content.append("ssh -i " + CLUSTERKEY + " " + ip + " ");
            content
                .append("sudo aws s3 cp /shared/")
                .append(userId)
                .append("/")
                .append(projectId)
                .append("/")
                .append(modelId)
                .append("/outputFiles/")
                .append(job.getMesh().getModel().getName().split("\\.")[0])
                .append(".drc")
                .append(" ")
                .append(S3_BUCKET_LINK)
                .append(outputLink.get("drc"));
            services.run(content.toString());

            jobCompletionDetails = getJobCompletionDetails(job.getJobId(), ip);
            job.setnCPUs(Integer.parseInt(jobCompletionDetails.get("nCPUs")));
            job.setElapsedTime(jobCompletionDetails.get("elapsedTime"));
            Mesh mesh = job.getMesh();
            mesh.setHasBeenSimulated(true);
            meshRepository.save(mesh);
            jobRepository.save(job);

            //Adding the job to particular list in the accounting entity.
            Accounts account = userRepository.findById(userId).get().getAccounting();
            Map<String, List<Job>> jobDetails = account.getJobList();
            Date date = new Date(job.getDatetime());
            Calendar cal = Calendar.getInstance();
            cal.setTime(date);
            String listName = "_" + cal.get(Calendar.MONTH) + "_" + cal.get(Calendar.YEAR);
            if (jobDetails.containsKey(listName)) {
                List<Job> jobList = jobDetails.get(listName);
                jobList.add(job);
            } else {
                List<Job> jobList = new ArrayList<>();
                jobList.add(job);
                jobDetails.put(listName, jobList);
            }
            account.setJobList(jobDetails);
            accountingRepository.save(account);
            jobRepository.delete(job);
            System.out.println("Completion Details:-");
            System.out.println("Elapsed Time:- " + jobCompletionDetails.get("elapsedTime"));
            System.out.println("No of CPUs:- " + jobCompletionDetails.get("nCPUs"));
            System.out.println("Job Deleted ");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public List<Job> getJobsByProjectId(String projectId) throws Exception {
        Optional<User> user = userRepository.findOneByLogin(userService.getUserWithAuthorities().get().getLogin());
        if (user.isPresent()) {
            Optional<Project> project = projectRepository.findById(projectId);
            if (project.isPresent()) {
                ObjectId id = new ObjectId(projectId);
                List<Job> jobs = jobRepository.getAllJobsByProject(id);
                return jobs;
            } else {
                throw new Exception("Project not found");
            }
        } else {
            throw new Exception("User not found");
        }
    }

    @Override
    public Job doJob(Geometry model, String edgeLength, String tolerance, String ip) {
        try {
            Job job = services.generateAndTransferScripts(model, edgeLength, tolerance, ip);
            job.setUser(model.getProject().getUser());
            job.setProject(model.getProject());
            Mesh mesh;
            Project project = model.getProject();
            List<Mesh> list = project.getMeshes();
            if (meshService.getMeshByGeometry(model.getId()).isPresent()) {
                System.out.println("Mesh already there");
                mesh = meshService.getMeshByGeometry(model.getId()).get();
                list.remove(mesh);
                System.out.println(list.size());
                mesh.setEdgeLength(Double.parseDouble(edgeLength));
                mesh.setTolerance(Double.parseDouble(tolerance));
                meshRepository.save(mesh);
            } else {
                System.out.println("Creating new mesh");
                mesh = meshService.createMesh(model, edgeLength, tolerance);
            }
            job.setMesh(mesh);
            jobRepository.save(job);

            list.add(mesh);
            System.out.println(list.size());
            project.setMeshes(list);
            projectRepository.save(project);

            String command =
                "ssh -i " +
                CLUSTERKEY +
                " " +
                ip +
                " bash /shared/" +
                job.getUser().getId() +
                "/" +
                job.getProject().getId() +
                "/" +
                job.getMesh().getModel().getId() +
                "/HeadScripts/main.sh";
            System.out.println(command);
            services.run(command);
            command =
                "ssh -i " +
                CLUSTERKEY +
                " " +
                ip +
                " cat /shared/" +
                job.getUser().getId() +
                "/" +
                job.getProject().getId() +
                "/" +
                job.getMesh().getModel().getId() +
                "/HeadScripts/job.txt ";
            System.out.println(command);
            String jobId = services.run(command).get("successfulOutput").split(" ")[3];
            System.out.println("Done");

            if (jobId != null) {
                job.setJobId(jobId);
                jobRepository.save(job);
                return job;
            } else {
                throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Job submission failed");
            }
        } catch (Exception e) {
            System.out.println("Error in the process...");
            System.out.println(e);
            return null;
        }
    }

    @Override
    public ResponseEntity simulate(String projectId, String modelId, GeometryDTO data) {
        System.out.println("In Simulate Service");
        Optional<User> user = userRepository.findOneByLogin(userService.getUserWithAuthorities().get().getLogin());
        if (user.isPresent()) {
            Optional<Project> project = projectRepository.findById(projectId);
            if (project.isPresent()) {
                Optional<Geometry> model = modelRepository.findById(modelId);
                if (model.isPresent()) {
                    Job job;
                    try {
                        services.createCluster();
                    } catch (Exception e) {
                        throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Request failed");
                    }
                    try {
                        String ip = services.getIP();
                        System.out.println("IP from ServiceImpl " + ip);

                        String edgeLength = data.getEdgeLength();
                        String tolerance = data.getTolerance();
                        String iteration = data.getIteration();

                        job = doJob(model.get(), edgeLength, tolerance, ip);
                        job.setJobStatus(getJobStatus(job.getJobId(), ip));
                        job.setDatetime(new Date().getTime());
                        jobRepository.save(job);
                    } catch (Exception e) {
                        e.printStackTrace();
                        throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Request failed");
                    }
                    return ResponseEntity.ok().body(job.getJobId());
                } else {
                    return ResponseEntity.notFound().build();
                }
            } else {
                return ResponseEntity.notFound().build();
            }
        } else {
            return ResponseEntity.notFound().build();
        }
    }

    @Override
    public Map<String, String> getJobCompletionDetails(String jobId, String ip) throws Exception {
        Map<String, String> details;
        StringBuilder command = new StringBuilder();

        command
            .append("ssh -i ")
            .append(CLUSTERKEY)
            .append(" ")
            .append(ip)
            .append(" ")
            .append("sacct --format=jobid,jobname,elapsed,ncpus,state -j ")
            .append(jobId);

        details = services.run(command.toString());

        String[] output = details.get("successfulOutput").trim().replaceAll("\\s+", "-").replaceAll("-+", "-->").split("-->");
        String elapsed = null, ncpus = null;
        int counter = 5;
        String line = output[counter];
        while (line != null) {
            if (line.equals(jobId) && output[++counter].equals("Meshing")) {
                elapsed = output[++counter];
                ncpus = output[++counter];
                break;
            }
            counter += 4;
            try {
                line = output[counter];
            } catch (ArrayIndexOutOfBoundsException e) {
                break;
            }
        }

        details = new HashMap<>();
        details.put("elapsedTime", elapsed);
        details.put("nCPUs", ncpus);
        return details;
    }
}
