package cc.ddrpa.dorian.polystash.utils.s3;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.commons.lang3.tuple.Pair;

import java.io.File;
import java.io.IOException;
import java.util.function.Supplier;

/**
 * MinIO 凭据 JSON 解析工具类，用于从 JSON 文件中解析 MinIO 访问凭据。
 * <p>
 * 该类提供了从 JSON 格式的凭据文件中解析 MinIO 访问密钥和秘密密钥的功能。
 * 返回一个 Supplier 函数，支持延迟加载凭据，避免在应用启动时立即读取文件。
 * <p>
 * 预期的 JSON 文件格式：
 * <pre>
 * {
 *   "accessKey": "your-access-key",
 *   "secretKey": "your-secret-key"
 * }
 * </pre>
 *
 * @see Pair
 * @see Supplier
 */
public class ParseMinIOCredentialFromJSON {

    private ParseMinIOCredentialFromJSON() {
        throw new UnsupportedOperationException("This class cannot be instantiated");
    }

    /**
     * 从 JSON 文件中解析 MinIO 凭据，返回凭据提供者函数。
     * <p>
     * 解析指定的 JSON 文件，提取 accessKey 和 secretKey 字段，
     * 返回一个 Supplier 函数，该函数在调用时会返回包含凭据的 Pair 对象。
     * <p>
     * 如果文件不存在、格式错误或缺少必需字段，将抛出 RuntimeException。
     * 建议在生产环境中添加适当的错误处理和重试机制。
     * <p>
     * 返回的 Pair 对象中：
     * <ul>
     *   <li>左值（left）：访问密钥（accessKey）</li>
     *   <li>右值（right）：秘密密钥（secretKey）</li>
     * </ul>
     *
     * @param keySetFile 包含 MinIO 凭据的 JSON 文件
     * @return 凭据提供者函数，调用时返回包含 accessKey 和 secretKey 的 Pair
     * @throws RuntimeException 当文件解析失败或缺少必需字段时抛出
     */
    public static Supplier<Pair<String, String>> parse(File keySetFile) {
        return () -> {
            ObjectMapper mapper = new ObjectMapper();
            JsonNode node = null;
            try {
                node = mapper.readTree(keySetFile);
            } catch (IOException e) {
                throw new RuntimeException(
                        String.format("Failed to parse MinIO credentials from JSON file '%s'", keySetFile.getAbsolutePath()), e);
            }
            return Pair.of(node.get("accessKey").asText(), node.get("secretKey").asText());
        };
    }
}
