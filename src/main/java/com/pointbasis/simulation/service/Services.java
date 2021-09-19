package com.pointbasis.simulation.service;

import static com.pointbasis.simulation.others.Constants.*;

import com.pointbasis.simulation.domain.Geometry;
import com.pointbasis.simulation.domain.Job;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileWriter;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.Map;
import org.bson.types.ObjectId;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

@Service
public class Services {

    @Autowired
    private Environment environment;

    public Map<String, String> run(String command) throws Exception {
        Process process = Runtime.getRuntime().exec(command);
        BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
        StringBuilder successfulOutput = new StringBuilder();
        String error = null;
        String line, pre = null;
        while ((line = reader.readLine()) != null) {
            if (!line.equals(pre)) {
                successfulOutput.append(line);
                System.out.println(line);
                pre = line;
            }
        }
        reader = new BufferedReader(new InputStreamReader(process.getErrorStream()));
        while ((line = reader.readLine()) != null) {
            error = line;
            System.out.println(line);
        }

        Map<String, String> output = new HashMap<>();
        output.put("successfulOutput", successfulOutput.toString());
        output.put("error", error);
        return output;
    }

    public void createCluster() throws Exception {
        System.out.println("Creating Cluster...");
        String command = null;
        try {
            if (environment.getActiveProfiles()[0].equals("prod")) command =
                "pcluster create PointBasis -c " + PRODCLUSTERCONFIG; else command = "pcluster create PointBasis -c " + DEVCLUSTERCONFIG;
        } catch (Exception e) {
            command = "pcluster create PointBasis -c " + DEVCLUSTERCONFIG;
        }
        run(command);
    }

    public ResponseEntity terminateCluster() {
        String command = "pcluster delete " + CLUSTERNAME;
        try {
            System.out.println("Terminating Cluster...");
            Thread thread = new Thread(
                () -> {
                    try {
                        run(command);
                    } catch (Exception e) {
                        System.out.println(e);
                    }
                }
            );
            thread.setDaemon(true);
            thread.start();
            return ResponseEntity.ok().build();
        } catch (Exception e) {
            System.out.println("Error terminating Cluster");
            return ResponseEntity.badRequest().build();
        }
    }

    public String getIP() throws Exception {
        System.out.println("Getting IP...");
        String command = "pcluster ssh " + CLUSTERNAME + " -d -i " + CLUSTERKEY;
        Process process = Runtime.getRuntime().exec(command);
        BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
        String line = reader.readLine();
        try {
            return line.split(" ")[3];
        } catch (Exception e) {
            return null;
        }
    }

    public Job generateAndTransferScripts(Geometry model, String edgeLength, String tolerance, String ip) throws Exception {
        System.out.println("Generating Scripts...");
        String userId = model.getProject().getUser().getId();
        String projectId = model.getProject().getId();
        String modelId = model.getId();
        Map<String, String> inputLink = model.getInputLink();
        Job job = new Job();
        ObjectId objectId = new ObjectId();
        job.setId(objectId.toHexString());

        File file = new File(HEADSCRIPTS + objectId.toHexString());
        if (!file.exists()) file.createNewFile();
        FileWriter writer = new FileWriter(file);
        StringBuilder command = new StringBuilder();
        StringBuilder content = new StringBuilder();

        /**
         * Storing directory structure
         * */
        content.setLength(0);
        content
            .append("sudo mkdir -m777 -p /shared/")
            .append(userId)
            .append("/")
            .append(projectId)
            .append("/")
            .append(modelId)
            .append("/{inputFiles,outputFiles,HeadScripts}");

        /**
         * Making directory structure
         * */
        command.setLength(0);
        command
            .append("ssh -i ")
            .append(CLUSTERKEY)
            .append(" -o StrictHostKeyChecking=no ")
            .append(ip)
            .append(" ")
            .append(content)
            .append("; sudo chown ec2-user /shared")
            .append("; sudo chmod 777 /shared");
        System.out.println(command);
        run(command.toString());

        /**
         * Generating downloadObject.sh
         * */
        content.setLength(0);
        content.append("#!/bin/bash\n");
        content
            .append("sudo aws s3 cp ")
            .append(S3_BUCKET_LINK)
            .append(inputLink.get("stl"))
            .append(" /shared/")
            .append(userId)
            .append("/")
            .append(projectId)
            .append("/")
            .append(modelId)
            .append("/")
            .append("inputFiles/");
        writer.write(content.toString());
        writer.close();

        /**
         * Transferring downloadObject.sh
         * */
        command.setLength(0);
        command
            .append("scp -C -i ")
            .append(CLUSTERKEY)
            .append(" ")
            .append(file.getAbsolutePath())
            .append(" ")
            .append(ip)
            .append(":/shared/")
            .append(userId)
            .append("/")
            .append(projectId)
            .append("/")
            .append(modelId)
            .append("/")
            .append("HeadScripts/downloadObject.sh");
        System.out.println(command);
        run(command.toString());

        /**
         * Generating submission.sbatch
         * */
        writer = new FileWriter(file);
        content.setLength(0);
        content.append("#!/bin/bash\n");
        content.append("#SBATCH --job-name=Meshing").append("\n");
        content.append("#SBATCH --ntasks=1\n");
        content.append("#SBATCH --output=%x_%j.out\n");
        content
            .append("srun sudo /shared/FloatTetwild_bin --lr ")
            .append(edgeLength)
            .append(" ")
            .append("--epsr ")
            .append(tolerance)
            .append(" ");
        content
            .append("--input /shared/")
            .append(userId)
            .append("/")
            .append(projectId)
            .append("/")
            .append(modelId)
            .append("/inputFiles/")
            .append(model.getName())
            .append(".stl ");
        content
            .append("--output /shared/")
            .append(userId)
            .append("/")
            .append(projectId)
            .append("/")
            .append(modelId)
            .append("/outputFiles/")
            .append(model.getName().split("\\.")[0])
            .append(".msh && ");
        content
            .append("sudo /shared/draco_encoder ")
            .append("-i /shared/")
            .append(userId)
            .append("/")
            .append(projectId)
            .append("/")
            .append(modelId)
            .append("/outputFiles/")
            .append(model.getName().split("\\.")[0])
            .append(".msh__sf.obj ");
        content
            .append("-o /shared/")
            .append(userId)
            .append("/")
            .append(projectId)
            .append("/")
            .append(modelId)
            .append("/outputFiles/")
            .append(model.getName().split("\\.")[0])
            .append(".drc -cl 10");

        writer.write(content.toString());
        writer.close();

        /**
         * Transferring submission.sbatch
         * */
        command.setLength(0);
        command
            .append("scp -C -i ")
            .append(CLUSTERKEY)
            .append(" ")
            .append(file.getAbsolutePath())
            .append(" ")
            .append(ip)
            .append(":/shared/")
            .append(userId)
            .append("/")
            .append(projectId)
            .append("/")
            .append(modelId)
            .append("/")
            .append("HeadScripts/submission.sbatch");
        System.out.println(command);
        run(command.toString());

        /**
         * Generating installUtilities.sh
         * */
        writer = new FileWriter(file);
        content.setLength(0);
        content.append("#!/bin/bash\n");
        content
            .append("sudo aws s3 cp ")
            .append(S3_BUCKET_LINK)
            .append("FloatTetwild_bin /shared && sudo chmod +x /shared/FloatTetwild_bin\n");
        content.append("sudo aws s3 cp ").append(S3_BUCKET_LINK).append("draco_encoder /shared && sudo chmod +x /shared/draco_encoder");
        writer.write(content.toString());
        writer.close();

        /**
         * Transferring installUtilities.sh
         * */
        command.setLength(0);
        command
            .append("scp -C -i ")
            .append(CLUSTERKEY)
            .append(" ")
            .append(file.getAbsolutePath())
            .append(" ")
            .append(ip)
            .append(":/shared/")
            .append(userId)
            .append("/")
            .append(projectId)
            .append("/")
            .append(modelId)
            .append("/")
            .append("HeadScripts/installUtilities.sh");
        System.out.println(command);
        run(command.toString());

        /**
         * Generating main.sh
         * */
        writer = new FileWriter(file);
        content.setLength(0);
        content.append("#!/bin/bash\n");
        content
            .append("bash /shared/")
            .append(userId)
            .append("/")
            .append(projectId)
            .append("/")
            .append(modelId)
            .append("/HeadScripts/installUtilities.sh\n");
        content
            .append("bash /shared/")
            .append(userId)
            .append("/")
            .append(projectId)
            .append("/")
            .append(modelId)
            .append("/HeadScripts/downloadObject.sh\n");
        content
            .append("install -m 666 <(sbatch /shared/")
            .append(userId)
            .append("/")
            .append(projectId)
            .append("/")
            .append(modelId)
            .append("/HeadScripts/submission.sbatch) /shared/")
            .append(userId)
            .append("/")
            .append(projectId)
            .append("/")
            .append(modelId)
            .append("/HeadScripts/job.txt");
        writer.write(content.toString());
        writer.close();

        /**
         * Transferring main.sh
         * */
        command.setLength(0);
        command
            .append("scp -C -i ")
            .append(CLUSTERKEY)
            .append(" ")
            .append(file.getAbsolutePath())
            .append(" ")
            .append(ip)
            .append(":/shared/")
            .append(userId)
            .append("/")
            .append(projectId)
            .append("/")
            .append(modelId)
            .append("/")
            .append("HeadScripts/main.sh");
        System.out.println(command);
        run(command.toString());

        file.delete();
        return job;
    }
}
