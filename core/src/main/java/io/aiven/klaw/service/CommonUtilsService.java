package io.aiven.klaw.service;

import io.aiven.klaw.config.ManageDatabase;
import io.aiven.klaw.dao.Topic;
import io.aiven.klaw.dao.UserInfo;
import io.aiven.klaw.helpers.UtilMethods;
import io.aiven.klaw.model.KwMetadataUpdates;
import io.aiven.klaw.model.charts.ChartsJsOverview;
import io.aiven.klaw.model.charts.Options;
import io.aiven.klaw.model.charts.Title;
import io.aiven.klaw.model.enums.EntityType;
import io.aiven.klaw.model.enums.MetadataOperationType;
import io.aiven.klaw.model.enums.PermissionType;
import java.io.*;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.Certificate;
import java.security.cert.CertificateException;
import java.security.cert.CertificateFactory;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.stream.Collectors;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.http.*;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.oauth2.core.user.DefaultOAuth2User;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

@Service
@Slf4j
public class CommonUtilsService {

  @Value("${klaw.enable.authorization.ad:false}")
  private String enableUserAuthorizationFromAD;

  @Value("${spring.security.oauth2.client.provider.klaw.user-name-attribute:preferred_username}")
  private String preferredUsername;

  @Value("${klaw.saas.ssl.clientcerts.location:./tmp/}")
  private String clientCertsLocation;

  @Value("${klaw.saas.ssl.clusterapi.truststore:./tmp}")
  private String trustStore;

  @Value("${klaw.saas.ssl.clusterapi.truststore.pwd:./tmp}")
  private String trustStorePwd;

  @Value("${klaw.metadata.kafkamode:false}")
  private boolean metadataKafkaMode;

  @Autowired ManageDatabase manageDatabase;

  //    @Autowired(required = false)
  //    KafkaProducerConsumerService kafkaProducerConsumerService;

  private static HttpComponentsClientHttpRequestFactory requestFactory =
      ClusterApiService.requestFactory;

  @Value("${klaw.uiapi.servers:server1,server2}")
  private String uiApiServers;

  @Value("${server.servlet.context-path:}")
  private String kwContextPath;

  private RestTemplate getRestTemplate() {
    if (uiApiServers.toLowerCase().startsWith("https")) return new RestTemplate(requestFactory);
    else return new RestTemplate();
  }

  public Authentication getAuthentication() {
    return SecurityContextHolder.getContext().getAuthentication();
  }

  String getAuthority(Object principal) {
    if ("true".equals(enableUserAuthorizationFromAD)) {
      if (principal instanceof DefaultOAuth2User) {
        DefaultOAuth2User defaultOAuth2User = (DefaultOAuth2User) principal;
        Object[] authorities = defaultOAuth2User.getAuthorities().toArray();
        if (authorities.length > 0) {
          return (String) authorities[0];
        } else {
          return "";
        }
      } else if (principal instanceof String) {
        return manageDatabase.getHandleDbRequests().getUsersInfo((String) principal).getRole();
      } else if (principal instanceof UserDetails) {
        Object[] authorities = ((UserDetails) principal).getAuthorities().toArray();
        if (authorities.length > 0) {
          SimpleGrantedAuthority sag = (SimpleGrantedAuthority) authorities[0];
          return sag.getAuthority();
        } else {
          return "";
        }

      } else {
        return "";
      }
    } else {
      UserInfo userInfo = manageDatabase.getHandleDbRequests().getUsersInfo(getUserName(principal));
      if (userInfo != null) {
        return userInfo.getRole();
      } else {
        return null;
      }
    }
  }

  public String getUserName(Object principal) {
    return UtilMethods.getUserName(principal, preferredUsername);
  }

  public String getCurrentUserName() {
    return UtilMethods.getUserName(preferredUsername);
  }

  public boolean isNotAuthorizedUser(Object principal, PermissionType permissionType) {
    try {
      return !manageDatabase
          .getRolesPermissionsPerTenant(getTenantId(getUserName(principal)))
          .get(getAuthority(principal))
          .contains(permissionType.name());
    } catch (Exception e) {
      log.debug(
          "Error isNotAuthorizedUser / Check if role exists. {} {} {}",
          getUserName(principal),
          permissionType.name(),
          getAuthority(getUserName(principal)),
          e);
      return true;
    }
  }

  public ChartsJsOverview getChartsJsOverview(
      List<Map<String, String>> activityCountList,
      String title,
      String yaxisCount,
      String xaxisLabel,
      String xAxisLabelConstant,
      String yAxisLabelConstant,
      int tenantId) {
    ChartsJsOverview chartsJsOverview = new ChartsJsOverview();
    List<Integer> data = new ArrayList<>();
    List<String> labels = new ArrayList<>();
    List<String> colors = new ArrayList<>();

    data.add(0);
    labels.add("");

    int totalCount = 0;

    if (activityCountList != null) {
      for (Map<String, String> hashMap : activityCountList) {
        totalCount += Integer.parseInt(hashMap.get(yaxisCount));
        data.add(Integer.parseInt(hashMap.get(yaxisCount)));

        if ("teamid".equals(xaxisLabel)) {
          labels.add(
              manageDatabase.getTeamNameFromTeamId(
                  tenantId, Integer.parseInt(hashMap.get(xaxisLabel))));
        } else {
          labels.add(hashMap.get(xaxisLabel));
        }

        colors.add("Green");
      }
    }
    chartsJsOverview.setData(data);
    chartsJsOverview.setLabels(labels);
    chartsJsOverview.setColors(colors);

    Options options = new Options();
    Title title1 = new Title();
    title1.setDisplay(true);
    title1.setText(title + " (Total " + totalCount + ")");
    title1.setPosition("bottom");
    title1.setFontColor("red");

    options.setTitle(title1);
    chartsJsOverview.setOptions(options);
    chartsJsOverview.setTitleForReport(title);

    chartsJsOverview.setXAxisLabel(xAxisLabelConstant);
    chartsJsOverview.setYAxisLabel(yAxisLabelConstant);

    return chartsJsOverview;
  }

  public String deriveCurrentPage(String pageNo, String currentPage, int totalPages) {
    switch (pageNo) {
      case ">":
        pageNo = (Integer.parseInt(currentPage) + 1) + "";
        break;
      case ">>":
        pageNo = totalPages + "";
        break;
      case "<":
        pageNo = (Integer.parseInt(currentPage) - 1) + "";
        break;
      case "<<":
        pageNo = "1";
        break;
    }
    return pageNo;
  }

  public void getAllPagesList(
      String pageNo, String currentPage, int totalPages, List<String> numList) {
    if (currentPage != null
        && !currentPage.equals("")
        && !currentPage.equals(pageNo)
        && Integer.parseInt(pageNo) > 1
        && totalPages > 1) {
      numList.add("<<");
      numList.add("<");
    } else if (currentPage != null
        && currentPage.equals(pageNo)
        && Integer.parseInt(pageNo) > 1
        && totalPages > 1) {
      numList.add("<<");
      numList.add("<");
    }

    if (totalPages > Integer.parseInt(pageNo)) {
      numList.add(pageNo);
      numList.add(">");
      numList.add(">>");
    } else if (totalPages == Integer.parseInt(pageNo)) {
      numList.add(pageNo);
    }
  }

  public boolean addPublicKeyToTrustStore(String fileName, Integer tenantId) {
    KeyStore ks;
    fileName = fileName + tenantId + ".pem";
    try {
      ks = KeyStore.getInstance("JKS");
      FileInputStream trustStoreStream = new FileInputStream(trustStore);

      ks.load(trustStoreStream, trustStorePwd.toCharArray());

      FileInputStream fis = new FileInputStream(clientCertsLocation + "/" + fileName);
      BufferedInputStream bis = new BufferedInputStream(fis);
      // I USE x.509 BECAUSE THAT'S WHAT keytool CREATES

      CertificateFactory cf = CertificateFactory.getInstance("X.509");
      Certificate cert;
      while (bis.available() > 0) {
        cert = cf.generateCertificate(bis);
        ks.setCertificateEntry(fileName, cert);
      }

      ks.store(new FileOutputStream(trustStore), trustStorePwd.toCharArray());
      return true;

    } catch (KeyStoreException | CertificateException | IOException | NoSuchAlgorithmException e) {
      log.error(
          "Unable to load public key to trust store clusterName: "
              + fileName
              + " Tenant "
              + tenantId
              + "-",
          e);
      return false;
    }
  }

  public void updateMetadata(
      int tenantId, EntityType entityType, MetadataOperationType operationType) {
    KwMetadataUpdates kwMetadataUpdates =
        KwMetadataUpdates.builder()
            .tenantId(tenantId)
            .entityType(entityType.name())
            .operationType(operationType.name())
            .createdTime(new Timestamp(System.currentTimeMillis()))
            .build();
    updateMetadata(kwMetadataUpdates);

    try {
      CompletableFuture.runAsync(
              () -> {
                resetCacheOnOtherServers(kwMetadataUpdates);
              })
          .get();
    } catch (InterruptedException | ExecutionException e) {
      log.error("Exception:", e);
    }
  }

  public synchronized void updateMetadata(KwMetadataUpdates kwMetadataUpdates) {
    final EntityType entityType = EntityType.of(kwMetadataUpdates.getEntityType());
    if (entityType == null) {
      return;
    }
    final MetadataOperationType operationType =
        MetadataOperationType.of(kwMetadataUpdates.getOperationType());
    if (entityType == EntityType.TEAM) {
      manageDatabase.loadEnvsForOneTenant(kwMetadataUpdates.getTenantId());
      manageDatabase.loadTenantTeamsForOneTenant(null, kwMetadataUpdates.getTenantId());
    } else if (entityType == EntityType.CLUSTER && operationType == MetadataOperationType.DELETE) {
      manageDatabase.deleteCluster(kwMetadataUpdates.getTenantId());
    } else if (entityType == EntityType.CLUSTER && operationType == MetadataOperationType.CREATE) {
      manageDatabase.loadClustersForOneTenant(null, null, null, kwMetadataUpdates.getTenantId());
    } else if (entityType == EntityType.ENVIRONMENT
        && operationType == MetadataOperationType.CREATE) {
      manageDatabase.loadEnvsForOneTenant(kwMetadataUpdates.getTenantId());
      manageDatabase.loadEnvMapForOneTenant(kwMetadataUpdates.getTenantId());
      manageDatabase.loadTenantTeamsForOneTenant(null, kwMetadataUpdates.getTenantId());
    } else if (entityType == EntityType.ENVIRONMENT
        && operationType == MetadataOperationType.DELETE) {
      manageDatabase.loadEnvMapForOneTenant(kwMetadataUpdates.getTenantId());
      manageDatabase.loadEnvsForOneTenant(kwMetadataUpdates.getTenantId());
    } else if (entityType == EntityType.TENANT && operationType == MetadataOperationType.CREATE) {
      manageDatabase.updateStaticDataForTenant(kwMetadataUpdates.getTenantId());
    } else if (entityType == EntityType.TENANT && operationType == MetadataOperationType.DELETE) {
      manageDatabase.deleteTenant(kwMetadataUpdates.getTenantId());
    } else if (entityType == EntityType.TENANT && operationType == MetadataOperationType.UPDATE) {
      manageDatabase.loadOneTenant(kwMetadataUpdates.getTenantId());
    } else if (entityType == EntityType.ROLES_PERMISSIONS) {
      manageDatabase.loadRolesPermissionsOneTenant(null, kwMetadataUpdates.getTenantId());
    } else if (entityType == EntityType.PROPERTIES) {
      manageDatabase.loadKwPropsPerOneTenant(null, kwMetadataUpdates.getTenantId());
    }
  }

  @Cacheable(cacheNames = "tenantsusernames", key = "#userId")
  public int getTenantId(String userId) {
    return manageDatabase.getHandleDbRequests().getUsersInfo(userId).getTenantId();
  }

  public String getLoginUrl() {
    return ServletUriComponentsBuilder.fromCurrentContextPath().build().toUriString() + "/login";
  }

  public String getBaseUrl() {
    if ("".equals(kwContextPath))
      return ServletUriComponentsBuilder.fromCurrentContextPath().build().toUriString();
    else
      return ServletUriComponentsBuilder.fromCurrentContextPath().build().toUriString()
          + "/"
          + kwContextPath;
  }

  public void resetCacheOnOtherServers(KwMetadataUpdates kwMetadataUpdates) {
    log.info("invokeResetEndpoints");
    try {
      String tenantName =
          manageDatabase
              .getHandleDbRequests()
              .getMyTenants(kwMetadataUpdates.getTenantId())
              .get()
              .getTenantName();

      // if property is null refresh memory directly
      if (uiApiServers != null && uiApiServers.length() > 0) {
        updateMetadata(kwMetadataUpdates);
      } else {
        String[] servers = new String[0];
        if (uiApiServers != null) {
          servers = uiApiServers.split(",");
        }

        String basePath;
        for (String server : servers) {

          if ("".equals(kwContextPath)) basePath = server;
          else basePath = server + "/" + kwContextPath;
          String uri =
              basePath
                  + "/resetMemoryCache/"
                  + tenantName
                  + "/"
                  + kwMetadataUpdates.getEntityType()
                  + "/"
                  + kwMetadataUpdates.getOperationType();
          RestTemplate restTemplate = getRestTemplate();

          HttpHeaders headers = new HttpHeaders();
          headers.setContentType(MediaType.APPLICATION_JSON);

          headers.add("Accept", MediaType.APPLICATION_JSON_VALUE);
          HttpEntity<String> entity = new HttpEntity<>(headers);

          restTemplate.exchange(uri, HttpMethod.GET, entity, String.class);
        }
      }

    } catch (Exception e) {
      log.error("Error from invokeResetEndpoints ", e);
    }
  }

  protected List<Topic> getFilteredTopicsForTenant(List<Topic> topicsFromSOT) {
    List<Topic> filteredList = new ArrayList<>();
    // tenant filtering
    try {
      final Set<String> allowedEnvIdSet = getEnvsFromUserId(getUserName(getPrincipal()));
      if (topicsFromSOT != null) {
        filteredList =
            topicsFromSOT.stream()
                .filter(topic -> allowedEnvIdSet.contains(topic.getEnvironment()))
                .collect(Collectors.toList());
      }
    } catch (Exception e) {
      // this situation cannot happen, as every topic has an assigned team and this flow is
      // triggered on topic overview, which means topic has an owner
      log.error("No environments/clusters found.", e);
    }
    return filteredList;
  }

  public Set<String> getEnvsFromUserId(String userName) {
    return new HashSet<>(
        manageDatabase.getTeamsAndAllowedEnvs(getTeamId(userName), getTenantId(userName)));
  }

  public Integer getTeamId(String userName) {
    return manageDatabase.getHandleDbRequests().getUsersInfo(userName).getTeamId();
  }

  public Object getPrincipal() {
    return SecurityContextHolder.getContext().getAuthentication().getPrincipal();
  }

  List<Topic> groupTopicsByEnv(List<Topic> topicsFromSOT) {
    List<Topic> tmpTopicList = new ArrayList<>();

    Map<String, List<Topic>> groupedList =
        topicsFromSOT.stream().collect(Collectors.groupingBy(Topic::getTopicname));
    groupedList.forEach(
        (k, v) -> {
          Topic t = v.get(0);
          List<String> tmpEnvList = new ArrayList<>();
          for (Topic topic : v) {
            tmpEnvList.add(topic.getEnvironment());
          }
          t.setEnvironmentsList(tmpEnvList);
          tmpTopicList.add(t);
        });
    return tmpTopicList;
  }
}
