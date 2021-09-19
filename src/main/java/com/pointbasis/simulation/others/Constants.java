package com.pointbasis.simulation.others;

import com.amazonaws.auth.AWSCredentials;
import com.amazonaws.auth.AWSStaticCredentialsProvider;
import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.regions.Regions;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3ClientBuilder;
import com.amazonaws.services.secretsmanager.AWSSecretsManager;
import com.amazonaws.services.secretsmanager.AWSSecretsManagerClientBuilder;
import com.amazonaws.services.secretsmanager.model.*;
import com.pointbasis.simulation.service.Services;
import java.io.File;
import org.json.JSONObject;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.secretsmanager.SecretsManagerClient;
import software.amazon.awssdk.services.secretsmanager.model.GetSecretValueResponse;

public class Constants {

    public static final String S3_BUCKET_LINK;
    public static final String S3_BUCKET_NAME;
    public static final String HEADSCRIPTS;
    public static final String SCRIPTS;
    public static final String userDir;
    public static final String PRODCLUSTERCONFIG;
    public static final String DEVCLUSTERCONFIG;
    public static final String CLUSTERNAME;
    public static final String CLUSTERKEY;
    public static final String OBJECT_PATH;
    public static final String DRACO_ENCODER_EXE;
    public static final String DRACO_DECODER_EXE;
    public static final String DRACO_ENCODER;
    public static final String DRACO_DECODER;
    public static final String DRACO_ENCODER_MAC;
    public static final String DRACO_DECODER_MAC;
    public static final String OS;
    public static final String AWS_REGION;
    public static final String AWS_ACCESS_KEY;
    public static final String AWS_SECRET_KEY;
    public static final AWSCredentials credentials;
    public static final AmazonS3 s3client;

    static {
        CLUSTERNAME = "PointBasis";
        AWS_REGION = "us-west-2";
        userDir = System.getProperty("user.dir");
        S3_BUCKET_NAME = "pointbasisofficial";
        S3_BUCKET_LINK = "s3://" + S3_BUCKET_NAME + "/";

        if (new File(userDir + "/src/main/resources").exists()) {
            HEADSCRIPTS = userDir + "/src/main/resources/HeadScripts/";
            SCRIPTS = userDir + "/src/main/resources/Scripts/";
            DRACO_ENCODER_EXE = userDir + "/src/main/resources/binaries/draco_encoder.exe ";
            DRACO_DECODER_EXE = userDir + "/src/main/resources/binaries/draco_decoder.exe ";
            DRACO_ENCODER = userDir + "/src/main/resources/binaries/draco_encoder ";
            DRACO_DECODER = userDir + "/src/main/resources/binaries/draco_decoder ";
            DRACO_ENCODER_MAC = userDir + "/src/main/resources/binaries/draco_encoder-1.4.1 ";
            DRACO_DECODER_MAC = userDir + "/src/main/resources/binaries/draco_decoder-1.4.1 ";
            PRODCLUSTERCONFIG = userDir + "/src/main/resources/parallelcluster/my-prod-cluster-config.ini";
            DEVCLUSTERCONFIG = userDir + "/src/main/resources/parallelcluster/my-dev-cluster-config.ini";
            CLUSTERKEY = userDir + "/src/main/resources/ssh/server.pem";
            OBJECT_PATH = userDir + "/src/main/resources/objects/";
        } else {
            StringBuilder command = new StringBuilder();
            Services services = new Services();
            try {
                command.append("sudo mkdir -m777 -p /tmp/resources/HeadScripts");
                services.run(command.toString());

                command.setLength(0);
                command.append("sudo mkdir -m777 -p /tmp/resources/objects");
                services.run(command.toString());

                command.setLength(0);
                command.append("sudo chmod 777 /app/resources/ssh/server.pem");
                services.run(command.toString());

                command.setLength(0);
                command.append("aws configure set default.region " + AWS_REGION);
                services.run(command.toString());

                command.setLength(0);
                command.append("sudo chmod +x /app/resources/binaries/draco_encoder");
                services.run(command.toString());

                command.setLength(0);
                command.append("sudo chmod +x /app/resources/binaries/draco_encoder-1.4.1");
                services.run(command.toString());
            } catch (Exception e) {
                e.printStackTrace();
                System.out.println("Cannot create directory in docker");
            }
            HEADSCRIPTS = "/tmp/resources/HeadScripts/";
            SCRIPTS = "/app/resources/Scripts/";
            DRACO_ENCODER_EXE = "/app/resources/binaries/draco_encoder.exe ";
            DRACO_DECODER_EXE = "/app/resources/binaries/draco_decoder.exe ";
            DRACO_ENCODER = "/app/resources/binaries/draco_encoder ";
            DRACO_DECODER = "/app/resources/binaries/draco_decoder ";
            DRACO_ENCODER_MAC = "/app/resources/binaries/draco_encoder-1.4.1 ";
            DRACO_DECODER_MAC = "/app/resources/binaries/draco_decoder-1.4.1 ";
            PRODCLUSTERCONFIG = "/app/resources/parallelcluster/my-prod-cluster-config.ini";
            DEVCLUSTERCONFIG = "/app/resources/parallelcluster/my-dev-cluster-config.ini";
            CLUSTERKEY = "/app/resources/ssh/server.pem";
            OBJECT_PATH = "/tmp/resources/objects/";
        }

        OS = System.getProperty("os.name").toLowerCase();
        JSONObject temp = getSecret();
        AWS_ACCESS_KEY = temp.get("ACCESS_KEY_ID").toString();
        AWS_SECRET_KEY = temp.get("SECRET_ACCESS_KEY").toString();
        credentials = new BasicAWSCredentials(AWS_ACCESS_KEY, AWS_SECRET_KEY);

        s3client =
            AmazonS3ClientBuilder
                .standard()
                .withCredentials(new AWSStaticCredentialsProvider(credentials))
                .withRegion(Regions.US_WEST_2)
                .build();
    }

    public static JSONObject getSecret() {
        String secretName = "SilmulationSecret";

//        SecretsManagerClient client = SecretsManagerClient.builder().region(region).build();
        AWSSecretsManager client = AWSSecretsManagerClientBuilder.standard().withRegion(AWS_REGION).build();

        String secret;
        GetSecretValueRequest getSecretValueRequest = new GetSecretValueRequest().withSecretId(secretName);
        GetSecretValueResult getSecretValueResponse = null;

        try {
            getSecretValueResponse = client.getSecretValue(getSecretValueRequest);
        } catch (DecryptionFailureException e) {
            throw e;
        } catch (InternalServiceErrorException e) {
            throw e;
        } catch (InvalidParameterException e) {
            throw e;
        } catch (InvalidRequestException e) {
            throw e;
        } catch (ResourceNotFoundException e) {
            throw e;
        }
        if (getSecretValueResponse.getSecretString() != null) {
            secret = getSecretValueResponse.getSecretString();
            try {
                return new JSONObject(secret);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return null;
    }
}
