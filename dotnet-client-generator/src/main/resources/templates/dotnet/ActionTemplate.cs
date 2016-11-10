        /// <summary>
        /// {{comment}}
        /// </summary>
        public {{resultName}} {{method}}({{paramName}} param)
        {
            param.Client = client;
            return ResourceBase.{{method}}<{{paramName}}, {{resultName}}>(param);
        }

        /// <summary>
        /// {{comment}}(Async)
        /// </summary>
        public Task<{{resultName}}> {{method}}Async({{paramName}} param)
        {
            param.Client = client;
            return ResourceBase.{{method}}Async<{{paramName}}, {{resultName}}>(param);
        }