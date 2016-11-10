using System.Threading.Tasks;

using RamlClient;

namespace {{namespace}}
{
    /// <summary>
    /// {{comment}}
    /// </summary>
    public class {{resourceName}}
    {
        private readonly ClientBase client;

        {{properties}}

        public {{resourceName}}(ClientBase client)
        {
            this.client = client;
            {{new properties}}
        }
        
        {{actions}}
    }
}