using RamlClient;

﻿namespace {{namespace}}
{
	/// <summary>
    /// {{comment}}
    /// </summary>
    public class {{clientName}} : ClientBase
    {
        {{properties}}

        private {{clientName}}()
        {
            BaseUri = "{{baseUri}}";
            ClientBase client = this;
            {{new properties}}
        }

        /// <summary>
        /// 创建客户端
        /// </summary>
        /// <returns></returns>
        public static {{clientName}} Create()
        {
            return new {{clientName}}();
        }
    }
}
