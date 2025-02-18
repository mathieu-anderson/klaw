import { EnvironmentDTO } from "src/domain/environment/environment-types";

const defaultEnvironmentDTO: EnvironmentDTO = {
  id: "1",
  name: "DEV",
  type: "kafka",
  tenantId: 101,
  topicprefix: null,
  topicsuffix: null,
  clusterId: 1,
  tenantName: "default",
  clusterName: "DEV",
  envStatus: "ONLINE",
  otherParams:
    "default.partitions=2,max.partitions=2,default.replication.factor=1,max.replication.factor=1,topic.prefix=,topic.suffix=",
  defaultPartitions: null,
  maxPartitions: null,
  defaultReplicationFactor: null,
  maxReplicationFactor: null,
  showDeleteEnv: false,
  totalNoPages: "1",
  allPageNos: ["1"],
};

function createMockEnvironmentDTO(
  environment: Partial<EnvironmentDTO>
): EnvironmentDTO {
  return { ...defaultEnvironmentDTO, ...environment };
}

export { createMockEnvironmentDTO };
