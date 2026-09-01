/**
 * Namespace Api
 *
 * All backend api type
 */
declare namespace Api {
  /**
   * namespace Cch
   *
   * backend api module: "Cch"
   */
  namespace Cch {
    /** challenge */
    type Challenge = Common.CommonRecord<{
      /** 主键 */
      id: CommonType.IdType;
      /** 题目编码（唯一业务编码） */
      code: string;
      /** 题目类型 */
      category: string;
      /** 题目名称 */
      name: string;
      /** 题目备注 */
      remark: string;
      /** 题目最新版ID */
      latestVersionId: CommonType.IdType;
      /** 题目状态（0-草稿中，1-已入库，2-已停用） */
      status: number;
      /** 是否已入库（latestVersionId 非空即已发版入库） */
      published: boolean;
      /** 最新版本号（关联版本表补充） */
      latestVersionTag: string;
      /** 删除标志 */
      delFlag: number;
    }>;

    /** challenge search params */
    type ChallengeSearchParams = CommonType.RecordNullable<
      Pick<
        Api.Cch.Challenge,
        | 'code'
        | 'category'
        | 'name'
        | 'remark'
        | 'status'
        | 'published'
      > & {
        /** 难度（最新草稿 config.difficulty，字典 cch_question_difficulty） */
        difficulty?: string;
        /** 知识点（最新草稿 config.knowledge 标签） */
        knowledge?: string;
      } &
      Api.Common.CommonSearchParams
    >;

    /** challenge operate params */
    type ChallengeOperateParams = CommonType.RecordNullable<
      Pick<
        Api.Cch.Challenge,
        | 'id'
        | 'category'
        | 'name'
        | 'remark'
      >
    >;

    /** challenge list */
    type ChallengeList = Api.Common.PaginatingQueryRecord<Challenge>;

    type ChallengeDraftConfig = {
      stem: string;
      difficulty: string;
      runType?: string;
      knowledge?: string[];
      attachments: ChallengeDraftConfigAttachment[];
      writeups?: ChallengeDraftConfigAttachment[];
      flags?: ChallengeDraftConfigFlag[];
      containerTargets?: ChallengeDraftContainerTarget[];
    }

    type ChallengeDraftContainerTargetResourceLimit = {
      cpuLimit?: number | null;
      memoryLimit?: number | null;
    }

    type ChallengeDraftContainerTargetPort = {
      protocol?: string | null;
      internalPort?: number | null;
      externalPort?: number | null;
      remark?: string | null;
    }

    type ChallengeDraftContainerTarget = {
      name?: string | null;
      imageId?: CommonType.IdType | null;
      imageName?: string | null;
      env?: Record<string, string>;
      ports?: Record<string, ChallengeDraftContainerTargetPort>;
      resources?: ChallengeDraftContainerTargetResourceLimit | null;
    }

    type ChallengeDraftConfigAttachment = {
      fileId: CommonType.IdType;
      fileName: string;
      fileUrl: string;
      remark: string | null;
    }

    /** Flag基类型 */
    type ChallengeDraftConfigFlag = {
      /** Flag类型：static(静态) 或 dynamic(动态) */
      type: 'static' | 'dynamic';
      /** 分值（用于分值推荐） */
      score?: number | null;
      /** Flag描述（给选手查看的） */
      description?: string | null;
      /** Flag备注（仅后台可见） */
      remark?: string | null;
    }

    /** 静态Flag */
    type ChallengeDraftConfigStaticFlag = ChallengeDraftConfigFlag & {
      type: 'static';
      /** Flag内容 */
      content?: string | null;
    }

    /** 动态Flag */
    type ChallengeDraftConfigDynamicFlag = ChallengeDraftConfigFlag & {
      type: 'dynamic';
      /** 动态Flag生成规则/配置 */
      generatorConfig?: string | null;
    }

    /** challenge draft */
    type ChallengeDraft = Common.CommonRecord<{
      /** 主键 */
      id: CommonType.IdType;
      /** 派生父草稿ID */
      parentId?: CommonType.IdType;
      /** 题目ID */
      challengeId: CommonType.IdType;
      /** 题目名称 */
      challengeName: string;
      /** 题目类型（基本信息，后端用于同步更新 Challenge） */
      challengeCategory?: string;
      /** 题目备注（基本信息，后端用于同步更新 Challenge） */
      challengeRemark?: string;
      /** 草稿描述 */
      challengeDescription: string;
      /** 配置 */
      config: ChallengeDraftConfig;
      /** 删除标志 */
      delFlag: number;
    }>;

    /** challenge draft search params */
    type ChallengeDraftSearchParams = CommonType.RecordNullable<
      Pick<
        Api.Cch.ChallengeDraft,
        | 'challengeId'
        | 'challengeName'
        | 'challengeDescription'
      > &
      Api.Common.CommonSearchParams
    >;

    /** challenge draft operate params */
    type ChallengeDraftOperateParams = CommonType.RecordNullable<
      Pick<
        Api.Cch.ChallengeDraft,
        | 'id'
        | 'challengeId'
        | 'challengeName'
        | 'challengeCategory'
        | 'challengeRemark'
        | 'challengeDescription'
        | 'config'
      >
    > & {
      /** 操作类型：edit-直接更新（不新增版本），save-保存时新增版本 */
      operateType?: 'edit' | 'save' | string;
    };

    /** challenge draft list */
    type ChallengeDraftList = Api.Common.PaginatingQueryRecord<ChallengeDraft>;

    /** challenge version */
    type ChallengeVersion = Common.CommonRecord<{
      /** 主键 */
      id: CommonType.IdType;
      /** 题目ID */
      challengeId: CommonType.IdType;
      /** 题目名称 */
      challengeName: string;
      /** 草稿ID */
      draftId: CommonType.IdType;
      /** 版本号 */
      versionTag: string;
      /** 版本描述 */
      versionDescription: string;
      /** 删除标志 */
      delFlag: number;
    }>;

    /** challenge version search params */
    type ChallengeVersionSearchParams = CommonType.RecordNullable<
      Pick<
        Api.Cch.ChallengeVersion,
        | 'challengeId'
        | 'challengeName'
        | 'draftId'
        | 'versionTag'
        | 'versionDescription'
      > &
      Api.Common.CommonSearchParams
    >;

    /** challenge version operate params */
    type ChallengeVersionOperateParams = CommonType.RecordNullable<
      Pick<
        Api.Cch.ChallengeVersion,
        | 'id'
        | 'challengeId'
        | 'challengeName'
        | 'draftId'
        | 'versionTag'
        | 'versionDescription'
      >
    >;

    /** challenge version list */
    type ChallengeVersionList = Api.Common.PaginatingQueryRecord<ChallengeVersion>;

    /** challenge version export task */
    type ChallengeVersionExportTask = Common.CommonRecord<{
      /** 主键 */
      id: CommonType.IdType;
      /** 题目版本ID */
      versionId: CommonType.IdType;
      /** 版本号 */
      versionTag: string;
      /** 题目名称 */
      challengeName?: string;
      /** 任务状态（0-待处理，1-处理中，2-已完成，3-失败） */
      taskStatus: 0 | 1 | 2 | 3;
      /** 重试次数 */
      retryCount?: number;
      /** 任务状态文本 */
      taskStatusText?: string;
      /** OSS文件ID */
      ossFileId?: CommonType.IdType;
      /** OSS文件名 */
      ossFileName?: string;
      /** 文件大小（字节） */
      fileSize?: number;
      /** 文件大小文本（格式化） */
      fileSizeText?: string;
      /** 临时下载链接 */
      downloadUrl?: string;
      /** 文件过期时间 */
      expireTime?: string;
      /** 错误信息 */
      errorMessage?: string;
    }>;

    /** export task search params */
    type ExportTaskSearchParams = CommonType.RecordNullable<
      Pick<
        Api.Cch.ChallengeVersionExportTask,
        | 'versionId'
        | 'versionTag'
        | 'taskStatus'
      > &
      Api.Common.CommonSearchParams
    >;

    /** export task list */
    type ExportTaskList = Api.Common.PaginatingQueryRecord<ChallengeVersionExportTask>;

    /** challenge file */
    type ChallengeFile = Common.CommonRecord<{
      /** 主键 */
      id: CommonType.IdType;
      /** 题目id */
      challengeId: CommonType.IdType;
      /** 文件名 */
      fileName: string;
      /** 原名 */
      originalName: string;
      /** 文件后缀名 */
      fileSuffix: string;
      /** URL地址 */
      url: string;
      /** 扩展字段 */
      ext1: string;
      /** 服务商 */
      service: string;
    }>;

    /** challenge file search params */
    type ChallengeFileSearchParams = CommonType.RecordNullable<
      Pick<
        Api.Cch.ChallengeFile,
        | 'challengeId'
        | 'fileName'
        | 'originalName'
        | 'fileSuffix'
        | 'url'
        | 'service'
      > &
      Api.Common.CommonSearchParams
    >;

    /** challenge file operate params */
    type ChallengeFileOperateParams = CommonType.RecordNullable<
      Pick<
        Api.Cch.ChallengeFile,
        | 'id'
        | 'challengeId'
        | 'fileName'
        | 'originalName'
        | 'fileSuffix'
        | 'url'
        | 'ext1'
        | 'service'
      >
    >;

    /** challenge file list */
    type ChallengeFileList = Api.Common.PaginatingQueryRecord<ChallengeFile>;

    /** container config */
    type ContainerConfig = Common.CommonRecord<{
      /** 主键 */
      id: CommonType.IdType;
      /** 配置名称 */
      configName: string;
      /** 后端类型（docker/kubernetes/registry） */
      backendType: 'docker' | 'kubernetes' | 'registry';
      /** Docker URL */
      dockerUrl?: string;
      /** Docker API版本 */
      dockerApiVersion?: string;
      /** Docker证书路径 */
      dockerCertPath?: string;
      /** Docker TLS验证（0否 1是） */
      dockerTlsVerify?: string;
      /** Kubernetes配置（JSON格式） */
      kubernetesConfig?: string;
      /** Kubernetes命名空间 */
      kubernetesNamespace?: string;
      /** Registry URL */
      registryUrl?: string;
      /** Registry用户名 */
      registryUsername?: string;
      /** Registry密码 */
      registryPassword?: string;
      /** Registry仓库（Repo） */
      registryRepo?: string;
      /** 状态（0正常 1停用） */
      status: Common.EnableStatus;
      /** 备注 */
      remark?: string;
    }>;

    /** container config search params */
    type ContainerConfigSearchParams = CommonType.RecordNullable<
      Pick<
        Api.Cch.ContainerConfig,
        | 'configName'
        | 'backendType'
      > &
      Api.Common.CommonSearchParams
    >;

    /** container config operate params */
    type ContainerConfigOperateParams = CommonType.RecordNullable<
      Pick<
        Api.Cch.ContainerConfig,
        | 'id'
        | 'configName'
        | 'backendType'
        | 'dockerUrl'
        | 'dockerApiVersion'
        | 'dockerCertPath'
        | 'dockerTlsVerify'
        | 'kubernetesConfig'
        | 'kubernetesNamespace'
        | 'registryUrl'
        | 'registryUsername'
        | 'registryPassword'
        | 'registryRepo'
        | 'status'
        | 'remark'
      >
    >;

    /** container config list */
    type ContainerConfigList = Api.Common.PaginatingQueryRecord<ContainerConfig>;

    /** Docker 容器信息 */
    type DockerContainer = {
      /** 容器ID */
      id: string;
      /** 容器名称 */
      names: string;
      /** 镜像名称 */
      image: string;
      /** 镜像ID */
      imageId: string;
      /** 命令 */
      command: string;
      /** 创建时间 */
      created: string;
      /** 状态 */
      status: string;
      /** 端口映射信息 */
      ports: string;
      /** 网络信息 */
      network: string;
      /** 使用的内存（字节） */
      memoryUsage?: number;
      /** CPU使用率 */
      cpuUsage?: number;
    }

    /** Docker 镜像信息 */
    type DockerImage = {
      /** 镜像ID */
      id: string;
      /** 镜像标签（仓库:标签格式） */
      repoTags: string;
      /** 仓库名称（不含标签） */
      repository: string;
      /** 标签 */
      tag: string;
      /** 镜像ID简写 */
      shortId: string;
      /** 大小（字节） */
      size: number;
      /** 大小（人类可读格式） */
      sizeHuman: string;
      /** 创建时间 */
      created: string;
      /** 镜像摘要 */
      digest?: string;
    }

    /** Docker 系统信息 */
    type DockerInfo = {
      /** Docker 版本 */
      serverVersion: number;
      /** 操作系统类型 */
      operatingSystem: string;
      /** 操作系统类型（详细） */
      operatingSystemType: string;
      /** 架构 */
      architecture: string;
      /** CPU 数量 */
      ncpu: number;
      /** 内存总量（字节） */
      memTotal: number;
      /** Docker 根目录 */
      dockerRootDir: string;
      /** 服务器名称 */
      name: string;
      /** 标签信息 */
      labels?: string[];
      /** 镜像数量 */
      images: number;
      /** 容器数量 */
      containers: number;
      /** 运行中的容器数量 */
      containersRunning: number;
      /** 暂停的容器数量 */
      containersPaused: number;
      /** 停止的容器数量 */
      containersStopped: number;
    }

    /** 集群节点信息（Docker Swarm 或 Kubernetes） */
    type ClusterNode = {
      /** 节点ID */
      id: string;
      /** 节点名称 */
      name: string;
      /** 节点角色（manager/worker 或 master/node） */
      role?: string;
      /** 节点状态 */
      status?: string;
      /** 节点地址 */
      address?: string;
      /** 节点标签（key-value格式） */
      labels?: Record<string, string>;
      /** 外部访问地址（从labels中提取） */
      externalAccessAddress?: string;
      /** 架构 */
      architecture?: string;
      /** 操作系统 */
      operatingSystem?: string;
      /** CPU数量 */
      cpuCount?: number;
      /** 内存总量（字节） */
      memoryTotal?: number;
    }

    /** challenge container image */
    type ChallengeContainerImage = Common.CommonRecord<{
      /** 主键 */
      id: CommonType.IdType;
      /** 题目ID */
      challengeId: CommonType.IdType;
      /** 镜像名称（展示用，一般为 name:tag 形式） */
      imageName: string;
      /** 镜像拉取地址（docker pull / 服务创建使用的完整地址，包含标签） */
      pullAddress?: string;
      /** 镜像大小(字节) */
      imageSize?: number;
      /** 镜像文件存储路径 */
      filePath: string;
      /** 镜像文件SHA256哈希值 */
      fileHash?: string;
      /** 上传状态(uploading:上传中,uploaded:已上传,validating:验证中,available:可用,error:错误) */
      status: 'uploading' | 'uploaded' | 'validating' | 'available' | 'error';
      /** 上传进度(百分比) */
      progress?: number;
      /** 错误信息 */
      errorMessage?: string;
    }>;

    /** challenge container image search params */
    type ChallengeContainerImageSearchParams = CommonType.RecordNullable<
      Pick<
        Api.Cch.ChallengeContainerImage,
        | 'challengeId'
        | 'imageName'
        | 'status'
      > &
      Api.Common.CommonSearchParams
    >;

    /** challenge container image operate params */
    type ChallengeContainerImageOperateParams = CommonType.RecordNullable<
      Pick<
        Api.Cch.ChallengeContainerImage,
        | 'id'
        | 'challengeId'
        | 'imageName'
        | 'imageSize'
        | 'pullAddress'
        | 'filePath'
        | 'fileHash'
        | 'status'
        | 'progress'
        | 'errorMessage'
      >
    >;

    /** challenge container image list */
    type ChallengeContainerImageList = Api.Common.PaginatingQueryRecord<ChallengeContainerImage>;

    /** contest stage */
    type ContestStage = {
      /** 阶段名称（如初赛、决赛、选拔赛等） */
      stageName?: string;
      /** 阶段开始时间 */
      startTime?: string | null;
      /** 阶段时长（分钟） */
      duration?: number;
      /** 本阶段赛题需求（多行字符串描述） */
      challengeRequirement?: string;
    };

    /** contest platform */
    type ContestPlatform = {
      /** 平台名称 */
      platformName?: string;
      /** 平台地址 */
      platformUrl?: string;
    };

    /** contest meta */
    type ContestMeta = {
      /** 竞赛名称 */
      contestName?: string;
      /** 赛事备注 */
      contestRemark?: string;
      /** 开始时间 */
      startTime?: string | undefined;
      /** 结束时间 */
      endTime?: string | undefined;
      /** 题目需求（多行字符串描述） */
      challengeRequirement?: string;
      /** 竞赛阶段列表（如初赛、决赛、选拔赛等） */
      stages?: ContestStage[];
      /** 竞赛平台列表 */
      platforms?: ContestPlatform[];
    };

    /** project */
    type Project = Common.CommonRecord<{
      /** 主键 */
      id: CommonType.IdType;
      /** 项目类型（'normal'普通项目, 'contest'竞赛项目） */
      projectType: 'normal' | 'contest';
      /** 项目名称 */
      name: string;
      /** 备注 */
      remark?: string;
      /** 竞赛meta信息（仅竞赛项目使用） */
      meta?: ContestMeta;
      /** 成员列表 */
      members?: ProjectMember[];
      /** 题目列表 */
      challenges?: ProjectChallenge[];
      /** 竞赛文件列表（仅竞赛项目） */
      contestFiles?: ContestFile[];
    }>;

    /** project search params */
    type ProjectSearchParams = CommonType.RecordNullable<
      Pick<
        Api.Cch.Project,
        | 'projectType'
        | 'name'
        | 'remark'
      > &
      Api.Common.CommonSearchParams
    >;

    /** project operate params */
    type ProjectOperateParams = CommonType.RecordNullable<
      Pick<
        Api.Cch.Project,
        | 'id'
        | 'projectType'
        | 'name'
        | 'remark'
        | 'meta'
      >
    >;

    /** project list */
    type ProjectList = Api.Common.PaginatingQueryRecord<Project>;

    /** project member */
    type ProjectMember = {
      /** 主键 */
      id: CommonType.IdType;
      /** 项目ID */
      projectId: CommonType.IdType;
      /** 用户ID */
      userId: CommonType.IdType;
      /** 权限类型（'admin'管理员, 'view_all'仅查看所有题, 'view_own'仅查看自己导入的题目） */
      permissionType: 'admin' | 'view_all' | 'view_own';
      /** 用户名（用于显示） */
      userName?: string;
      /** 用户昵称（用于显示） */
      nickName?: string;
      /** 创建时间 */
      createTime?: string;
    };

    /** project challenge */
    type ProjectChallenge = {
      /** 主键 */
      id: CommonType.IdType;
      /** 项目ID */
      projectId: CommonType.IdType;
      /** 题目ID */
      challengeId: CommonType.IdType;
      /** 题目版本ID */
      versionId: CommonType.IdType;
      /** 项目名称（用于显示） */
      projectName?: string;
      /** 题目名称（用于显示） */
      challengeName?: string;
      /** 版本号（用于显示） */
      versionTag?: string;
      /** 创建时间 */
      createTime?: string;
      /** 创建人 */
      createBy?: CommonType.IdType;
      /** 创建人名称（用于显示） */
      createByName?: string;
    };

    /** contest file */
    type ContestFile = {
      /** 主键 */
      id: CommonType.IdType;
      /** 项目ID（竞赛项目） */
      projectId: CommonType.IdType;
      /** OSS文件ID */
      ossId: CommonType.IdType;
      /** 文件标签 */
      fileTag?: string;
      /** 文件名（用于显示） */
      fileName?: string;
      /** 原始文件名（用于显示） */
      originalName?: string;
      /** 文件URL（用于显示） */
      url?: string;
      /** 创建时间 */
      createTime?: string;
    };

    /** dashboard name value */
    type DashboardNameValue = {
      name: string;
      value: number;
    };

    /** dashboard overview */
    type DashboardOverview = {
      projectCount: number;
      challengeCount: number;
      versionCount: number;
      draftCount: number;
      fileCount: number;
      imageCount: number;
      mockTestCount: number;
      exportTaskCount: number;
      projectChallengeCount: number;
      projectMemberCount: number;
      contestFileCount: number;
    };

    /** dashboard trend item */
    type DashboardTrendItem = {
      month: string;
      challengeCount: number;
      versionCount: number;
      projectCount: number;
    };

    /** dashboard statistics */
    type DashboardStatistics = {
      overview: DashboardOverview;
      categoryDistribution: DashboardNameValue[];
      projectTypeDistribution: DashboardNameValue[];
      imageStatusDistribution: DashboardNameValue[];
      exportTaskStatusDistribution: DashboardNameValue[];
      trend: DashboardTrendItem[];
      recentProjects: Project[];
    };
  }
}
