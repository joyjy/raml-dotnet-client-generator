using System.Net;

namespace RamlClient
{
    /// <summary>
    /// 接口结果通用定义
    /// </summary>
    public abstract class ResponseBase
    {
        /// <summary>
        /// 本次请求的 Http 状态码
        /// </summary>
        public HttpStatusCode StatusCode { get; set; }

        /// <summary>
        /// 操作结果状态
        /// </summary>
        public string OriginalContent { get; set; }
    }
}