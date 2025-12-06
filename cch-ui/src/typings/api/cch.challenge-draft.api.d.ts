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
      config: string;
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
  }
}
