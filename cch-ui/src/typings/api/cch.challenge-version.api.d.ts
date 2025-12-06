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
