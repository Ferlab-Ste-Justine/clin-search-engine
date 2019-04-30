package org.chursj.search.poc;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import javax.ws.rs.HttpMethod;

import org.elasticsearch.common.settings.Settings;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

import io.searchbox.client.JestClient;
import io.searchbox.client.JestClientFactory;
import io.searchbox.client.JestResult;
import io.searchbox.client.config.HttpClientConfig;
import io.searchbox.core.DocumentResult;
import io.searchbox.core.Index;
import io.searchbox.indices.CreateIndex;

public class PdfContentIndexer {

	@SuppressWarnings({ "unchecked", "unused" })
	public boolean doIndex(ExtractContentBean content, String serverUrl) throws IOException {
		JestClientFactory factory = new JestClientFactory();
        factory.setHttpClientConfig(new HttpClientConfig.Builder("http://142.1.177.88:8082")
                .defaultCredentials("admin", "admin")
                .multiThreaded(true)
                    .build());
        JestClient client = factory.getObject();
        Settings.Builder settingsBuilder = Settings.builder();
        settingsBuilder.put("number_of_shards", 5);
        settingsBuilder.put("number_of_replicas", 1);
        client.execute(new CreateIndex.Builder("searchcontent").settings(settingsBuilder.build().getAsStructuredMap()).build());
       
        Index index = new Index.Builder(content).index("searchcontent").type("pdfdata").build();
        DocumentResult result = client.execute(index);
        System.out.println("CREATED ===> " + result.isSucceeded());
        return result.isSucceeded();

	}
	public boolean indexer(ExtractContentBean content, String serverUrl) {
		try {
			// build Es client factory.
			Map<String, Object> settings = new HashMap<>();
			settings.put("number_of_shards", 8);

			// String mappingsJson = "{\"type1\":
			// {\"_source\":{\"enabled\":false},\"properties\":{\"field1\":{\"type\":\"keyword\"}}}}";

			final JestClientFactory factory = new JestClientFactory();

			HttpClientConfig httpCfg = new HttpClientConfig.Builder(serverUrl).multiThreaded(true)
					.defaultMaxTotalConnectionPerRoute(4).defaultCredentials("admin", "admin").maxTotalConnection(20)
					.build();
			factory.setHttpClientConfig(httpCfg);

			final JestClient client = factory.getObject();
			final CreateIndex createIndex = new CreateIndex.Builder("searchcontent").settings(settings)
					// .mappings(mappingsJson)
					.payload((Map<String, Object>) new ObjectMapper().convertValue(content,
							new TypeReference<Map<String, Object>>() {
							}))
					.build();

			final Index createIndexo = new Index.Builder(content)
                    .index("searchcontent")
                    //.type("searchcontent")
                   // .id(content.getFile())
                    .setHeader("Method", HttpMethod.PUT)
                    .build();
			
			JestResult result;
			result = client.execute(createIndexo);
			return result.isSucceeded();

		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return false;
		}

	}
}
