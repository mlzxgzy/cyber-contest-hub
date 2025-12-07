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
  }
}
