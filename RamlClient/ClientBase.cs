using System;

namespace RamlClient
{
    /// <summary>
    /// 客户端实例
    /// </summary>
    public class ClientBase
    {
        /// <summary>
        /// 站点 URI
        /// </summary>
        public string BaseUri { get; set; }

        /// <summary>
        /// 为 Uri 进行签名
        /// </summary>
        /// <param name="uri"></param>
        /// <returns></returns>
        public virtual string Sign(string uri)
        {
            return uri;
        }
    }
}