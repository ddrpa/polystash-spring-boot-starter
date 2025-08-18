package cc.ddrpa.dorian.polystash.utils.http;

import org.apache.commons.lang3.StringUtils;

import java.net.URLDecoder;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

/**
 * HTTP Content-Disposition 头部工具类，用于处理文件下载和显示相关的 HTTP 头部。
 * <p>
 * 该类提供了创建和解析 Content-Disposition HTTP 头部的功能，支持两种主要的
 * 内容处理方式：inline（内联显示）和 attachment（附件下载）。
 * </p>
 * <p>
 * 主要功能包括：
 * <ul>
 *   <li>生成 inline 类型的 Content-Disposition 头部</li>
 *   <li>生成 attachment 类型的 Content-Disposition 头部，支持文件名编码</li>
 *   <li>解析 Content-Disposition 头部，提取文件名信息</li>
 *   <li>自动处理文件名中的特殊字符，确保 HTTP 头部格式正确</li>
 * </ul>
 * <p>
 * 支持 RFC 6266 标准，包括 UTF-8 编码的文件名和 URL 编码处理。
 *
 * @see <a href="https://tools.ietf.org/html/rfc6266">RFC 6266 - Use of the Content-Disposition Header Field</a>
 */
public class ContentDisposition {

    /**
     * inline 内容处理类型。
     * <p>
     * 表示内容应该内联显示，通常用于在浏览器中直接显示文件内容，
     * 如图片、PDF 等。
     */
    private static final String INLINE = "inline";

    /**
     * attachment 内容处理类型。
     * <p>
     * 表示内容应该作为附件下载，通常用于文件下载场景。
     */
    private static final String ATTACHMENT = "attachment";

    private ContentDisposition() {
        throw new UnsupportedOperationException("ContentDisposition is a utility class and cannot be instantiated.");
    }

    /**
     * 生成 inline 类型的 Content-Disposition 头部。
     * <p>
     * 返回简单的 "inline" 字符串，用于指示浏览器内联显示内容。
     *
     * @return "inline" 字符串
     */
    public static String inline() {
        return INLINE;
    }

    /**
     * 生成 attachment 类型的 Content-Disposition 头部。
     * <p>
     * 根据文件名自动选择合适的格式：
     * <ul>
     *   <li>如果文件名为空，返回简单的 "attachment"</li>
     *   <li>如果文件名包含需要编码的特殊字符，使用 UTF-8 编码格式</li>
     *   <li>如果文件名安全，使用标准的双引号包围格式</li>
     * </ul>
     * <p>
     * 特殊字符包括：控制字符（ASCII 0-31）、双引号、百分号、尖括号、
     * 反斜杠、脱字符、反引号、大括号、竖线等。
     * </p>
     *
     * @param filename 要设置的文件名，可以为 null 或空字符串
     * @return 格式化的 Content-Disposition 头部字符串
     */
    public static String attachment(String filename) {
        if (StringUtils.isBlank(filename)) {
            return ATTACHMENT;
        }
        // if filename contains any character that should be encoded in URL, then encode it
        if (filename.chars().anyMatch(
                c -> c < 32 || c > 126 || c == 34 || c == 37 || c == 60 || c == 62 || c == 92 || c == 94
                        || c == 96 || c == 123 || c == 124 || c == 125)) {
            return String.format("attachment; filename*=UTF-8''%s",
                    URLEncoder.encode(filename, StandardCharsets.UTF_8));
        }
        return String.format("attachment; filename=\"%s\"", filename);
    }

    /**
     * 从 Content-Disposition 头部中解析文件名。
     * <p>
     * 支持多种文件名格式：
     * <ul>
     *   <li>标准格式：filename="filename.ext"</li>
     *   <li>UTF-8 编码格式：filename*=UTF-8''encoded-filename</li>
     *   <li>简单格式：filename=filename.ext</li>
     * </ul>
     * <p>
     * 如果头部为空或未找到文件名，返回 null。
     *
     * @param contentDispositionInHeader Content-Disposition 头部的完整字符串
     * @return 解析出的文件名，如果未找到则返回 null
     */
    public static String parseFilename(String contentDispositionInHeader) {
        if (StringUtils.isBlank(contentDispositionInHeader)) {
            return null;
        }
        String[] parts = contentDispositionInHeader.split(";");
        for (String part : parts) {
            part = part.trim();
            if (part.startsWith("filename=")) {
                String filename = part.substring(9).trim();
                if (filename.startsWith("\"") && filename.endsWith("\"")) {
                    return filename.substring(1, filename.length() - 1);
                } else if (filename.startsWith("UTF-8''")) {
                    return URLDecoder.decode(filename.substring(7), java.nio.charset.StandardCharsets.UTF_8);
                } else {
                    return filename;
                }
            }
        }
        return null;
    }
}
