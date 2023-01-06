package uk.gov.homeoffice.digital.sas.mutualtls.kafka;

import com.amazonaws.services.acmpca.AWSACMPCA;
import com.amazonaws.services.acmpca.model.AWSACMPCAException;
import com.amazonaws.services.acmpca.model.GetCertificateRequest;
import com.amazonaws.services.acmpca.model.GetCertificateResult;
import com.amazonaws.services.acmpca.model.InvalidArnException;
import com.amazonaws.services.acmpca.model.InvalidStateException;
import com.amazonaws.services.acmpca.model.RequestFailedException;
import com.amazonaws.services.acmpca.model.RequestInProgressException;
import com.amazonaws.services.acmpca.model.ResourceNotFoundException;
import com.amazonaws.waiters.Waiter;
import com.amazonaws.waiters.WaiterParameters;
import com.amazonaws.waiters.WaiterTimedOutException;
import com.amazonaws.waiters.WaiterUnrecoverableException;
import java.io.IOException;
import java.security.GeneralSecurityException;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.CertificateException;

public class GetCertificate {


  public String getCertificateRequest(String pcaArn, String certArn, String accessKey,
                                      String secretKey) throws IOException,
      GeneralSecurityException {
    GetCertificateRequest request = new GetCertificateRequest();
    request.withCertificateArn(certArn);
    request.withCertificateAuthorityArn(pcaArn);
    return createCertificateWaiter(request, accessKey, secretKey);
  }

  private String createCertificateWaiter(GetCertificateRequest request, String accessKey,
                                         String secretKey) throws
      CertificateException, KeyStoreException, IOException, NoSuchAlgorithmException {
    AwsClient awsClient = new AwsClient();
    AWSACMPCA client = awsClient.createAwsClient(accessKey, secretKey);
    Waiter<GetCertificateRequest> waiter = client.waiters().certificateIssued();
    try {
      waiter.run(new WaiterParameters<>(request));
    } catch (WaiterUnrecoverableException|WaiterTimedOutException|AWSACMPCAException e) {
      throw e;
    }

    return retrieveCertificateAndChain(request, client);
  }

  private String retrieveCertificateAndChain(GetCertificateRequest request,
                                             AWSACMPCA client) {
    GetCertificateResult result = null;
    try {
      result = client.getCertificate(request);
    } catch (RequestInProgressException|
             RequestFailedException|
             ResourceNotFoundException|
             InvalidArnException|
             InvalidStateException ex) {
      throw ex;
    }
    String certificate = result.getCertificate();
    String chain = result.getCertificateChain();
    return certificate + chain;
  }
}
