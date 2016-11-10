using System.Collections.Generic;
using System.Net.Http;

using RamlClient;

namespace {{namespace}}
{	
	/// <summary>
    /// {{comment}}
    /// </summary>
    public class {{paramName}} : RequestBase
    {
        {{properties}}
        
        /// <summary>
        /// HTTP 请求的 Path
        /// </summary>
        protected override string ResourcePath
        {
            get { return "{{path}}"; }
        }

        /// <summary>
        /// HTTP 请求的 QueryString
        /// </summary>
        protected override string QueryString
        {
            get { return {{queryStrings}}; }
        }

        /// <summary>
        /// HTTP 请求的 Headers
        /// </summary>
        protected internal override Dictionary<string, string> Headers
        {
            get { {{headers#return new Dictionary<string, string>();}} }
        }

        /// <summary>
        /// HTTP 请求的 Content
        /// </summary>
        protected internal override HttpContent Content
        {
            get { return {{content#null}}; }
        }
    }
}