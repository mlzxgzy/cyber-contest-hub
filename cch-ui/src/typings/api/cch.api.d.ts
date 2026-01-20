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
            /** 题目类型 */
            category: string;
            /** 题目名称 */
            name: string;
            /** 题目备注 */
            remark: string;
            /** 题目最新版ID */
            latestVersionId: CommonType.IdType;
            /** 删除标志 */
            delFlag: number;
        }>;

        /** challenge search params */
        type ChallengeSearchParams = CommonType.RecordNullable<
            Pick<
                Api.Cch.Challenge,
                | 'category'
                | 'name'
                | 'remark'
            > &
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
            knowledge?: string[];
            attachments: ChallengeDraftConfigAttachment[];
            writeups?: ChallengeDraftConfigAttachment[];
            flags?: ChallengeDraftConfigFlag[];
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
        >;

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
    }
}
