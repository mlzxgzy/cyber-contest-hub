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
            description: string;
            level: string;
        }

        /** challenge draft */
        type ChallengeDraft = Common.CommonRecord<{
            /** 主键 */
            id: CommonType.IdType;
            /** 题目ID */
            challengeId: CommonType.IdType;
            /** 题目名称 */
            challengeName: string;
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
    }
}
