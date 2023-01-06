package uk.gov.homeoffice.digital.sas.mutualtls.kafka;

import com.amazonaws.auth.AWSCredentials;
import com.amazonaws.auth.AWSStaticCredentialsProvider;
import com.amazonaws.auth.SystemPropertiesCredentialsProvider;
import com.amazonaws.services.acmpca.AWSACMPCA;
import com.amazonaws.services.acmpca.AWSACMPCAClientBuilder;
import uk.gov.homeoffice.digital.sas.mutualtls.constants.Constants;

public class AwsClient {

  public AWSACMPCA createAwsClient(String accessKey, String secretKey) {
    AWSCredentials credentials = getAwsCredentials(accessKey, secretKey);
    String endpointRegion = Constants.AWS_REGION;
    // Create a client that you can use to make requests.
    return AWSACMPCAClientBuilder.standard()
        .withRegion(endpointRegion)
        .withCredentials(new AWSStaticCredentialsProvider(credentials))
        .build();
  }

  private AWSCredentials getAwsCredentials(String accessKey, String secretKey) {
    System.setProperty("aws.accessKeyId", accessKey);
    System.setProperty("aws.secretKey", secretKey);
    return new SystemPropertiesCredentialsProvider().getCredentials();
  }
}
