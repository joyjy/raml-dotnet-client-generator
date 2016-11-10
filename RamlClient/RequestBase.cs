using System.Collections.Generic;
using System.Net.Http;

namespace RamlClient
{
    /// <summary>
    /// 发起请求
    /// </summary>
    public abstract class RequestBase
    {
        /// <summary>
        /// 所属 Client 的引用
        /// </summary>
        public ClientBase Client { protected get; set; }

        /// <summary>
        /// 接口地址 = Client.Sign(Client.BaseUri + ResourcePath + QueryString)
        /// </summary>
        protected internal string Uri {
            get { return Client.Sign(Client.BaseUri + ResourcePath + QueryString); }
        }

        /// <summary>
        /// HTTP 请求的 Path
        /// </summary>
        protected abstract string ResourcePath { get; }

        /// <summary>
        /// HTTP 请求的 QueryString
        /// </summary>
        protected abstract string QueryString { get; }

        /// <summary>
        /// HTTP 请求的 Headers
        /// </summary>
        protected internal abstract Dictionary<string, string> Headers { get; }

        /// <summary>
        /// HTTP 请求的 Content
        /// </summary>
        protected internal abstract HttpContent Content { get; }
    }
}