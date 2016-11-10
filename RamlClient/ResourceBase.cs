using System;
using System.Net;
using System.Net.Http;
using System.Threading.Tasks;
using Newtonsoft.Json;

namespace RamlClient
{
    /// <summary>
    /// 资源请求工具方法
    /// </summary>
    public static class ResourceBase
    {
        /// <summary>
        /// HTTP GET
        /// </summary>
        /// <typeparam name="TRequest"></typeparam>
        /// <typeparam name="TResponse"></typeparam>
        /// <param name="request"></param>
        /// <returns></returns>
        public static TResponse Get<TRequest, TResponse>(TRequest request)
            where TRequest : RequestBase
            where TResponse : ResponseBase
        {
            return GetAsync<TRequest, TResponse>(request).Result;
        }

        /// <summary>
        /// HTTP GET Async
        /// </summary>
        /// <typeparam name="TRequest"></typeparam>
        /// <typeparam name="TResponse"></typeparam>
        /// <param name="request"></param>
        /// <returns></returns>
        public static Task<TResponse> GetAsync<TRequest, TResponse>(TRequest request)
            where TRequest : RequestBase
            where TResponse : ResponseBase
        {
            return SendAsync<TRequest, TResponse>(HttpMethod.Get, request);
        }

        /// <summary>
        /// HTTP POST
        /// </summary>
        /// <typeparam name="TRequest"></typeparam>
        /// <typeparam name="TResponse"></typeparam>
        /// <param name="request"></param>
        /// <returns></returns>
        public static TResponse Post<TRequest, TResponse>(TRequest request)
            where TRequest : RequestBase
            where TResponse : ResponseBase
        {
            return PostAsync<TRequest, TResponse>(request).Result;
        }

        /// <summary>
        /// HTTP POST Async
        /// </summary>
        /// <typeparam name="TRequest"></typeparam>
        /// <typeparam name="TResponse"></typeparam>
        /// <param name="request"></param>
        /// <returns></returns>
        public static Task<TResponse> PostAsync<TRequest, TResponse>(TRequest request)
            where TRequest : RequestBase
            where TResponse : ResponseBase
        {
            return SendAsync<TRequest, TResponse>(HttpMethod.Post, request);
        }

        /// <summary>
        /// HTTP PUT
        /// </summary>
        /// <typeparam name="TRequest"></typeparam>
        /// <typeparam name="TResponse"></typeparam>
        /// <param name="request"></param>
        /// <returns></returns>
        public static TResponse Put<TRequest, TResponse>(TRequest request)
            where TRequest : RequestBase
            where TResponse : ResponseBase
        {
            return PutAsync<TRequest, TResponse>(request).Result;
        }

        /// <summary>
        /// HTTP PUT Async
        /// </summary>
        /// <typeparam name="TRequest"></typeparam>
        /// <typeparam name="TResponse"></typeparam>
        /// <param name="request"></param>
        /// <returns></returns>
        public static Task<TResponse> PutAsync<TRequest, TResponse>(TRequest request)
            where TRequest : RequestBase
            where TResponse : ResponseBase
        {
            return SendAsync<TRequest, TResponse>(HttpMethod.Put, request);
        }

        /// <summary>
        /// HTTP DELETE
        /// </summary>
        /// <typeparam name="TRequest"></typeparam>
        /// <typeparam name="TResponse"></typeparam>
        /// <param name="request"></param>
        /// <returns></returns>
        public static TResponse Delete<TRequest, TResponse>(TRequest request)
            where TRequest : RequestBase
            where TResponse : ResponseBase
        {
            return DeleteAsync<TRequest, TResponse>(request).Result;
        }

        /// <summary>
        /// HTTP DELETE Async
        /// </summary>
        /// <typeparam name="TRequest"></typeparam>
        /// <typeparam name="TResponse"></typeparam>
        /// <param name="request"></param>
        /// <returns></returns>
        public static Task<TResponse> DeleteAsync<TRequest, TResponse>(TRequest request)
            where TRequest : RequestBase
            where TResponse : ResponseBase
        {
            return SendAsync<TRequest, TResponse>(HttpMethod.Get, request);
        }

        /// <summary>
        /// 发起 HTTP 请求
        /// </summary>
        /// <typeparam name="TRequest"></typeparam>
        /// <typeparam name="TResponse"></typeparam>
        /// <param name="method"></param>
        /// <param name="request"></param>
        /// <returns></returns>
        private static Task<TResponse> SendAsync<TRequest, TResponse>(HttpMethod method, TRequest request)
            where TRequest : RequestBase
            where TResponse : ResponseBase
        {
            var client = new HttpClient();

            foreach (var header in request.Headers)
            {
                client.DefaultRequestHeaders.Add(header.Key, header.Value);
            }

            Task<HttpResponseMessage> t;
            if (method == HttpMethod.Get)
            {
                t = client.GetAsync(request.Uri);
            }
            else if(method == HttpMethod.Post)
            {
                t = client.PostAsync(request.Uri, request.Content);
            }
            else if(method == HttpMethod.Put)
            {
                t = client.PutAsync(request.Uri, request.Content);
            }
            else if (method == HttpMethod.Delete)
            {
                t = client.DeleteAsync(request.Uri);
            }
            else
            {
                throw new NotSupportedException(method.Method + " is not supported.");
            }

            return t.ContinueWith(last =>
                {
                    try
                    {
                        var responseMessage = last.Result;
                        var content = responseMessage.Content.ReadAsStringAsync().Result;
                        try
                        {
                            var response = JsonConvert.DeserializeObject<TResponse>(content);
                            response.StatusCode = responseMessage.StatusCode;
                            response.OriginalContent = content;
                            return response;
                        }
                        catch (JsonReaderException)
                        {
                            var response = (TResponse) Activator.CreateInstance(typeof (TResponse), new object[0]);
                            response.StatusCode = HttpStatusCode.InternalServerError;
                            response.OriginalContent = content;
                            return response;
                        }
                    }
                    finally
                    {
                        client.Dispose();
                    }
                });
        }
    }
}
