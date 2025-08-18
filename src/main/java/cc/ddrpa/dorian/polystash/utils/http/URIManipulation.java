package cc.ddrpa.dorian.polystash.utils.http;

import java.net.URI;

public class URIManipulation {

    public static URI uri(String hostWithScheme, String... path) {
        return URI.create(hostWithScheme).resolve(String.join("/", path));
    }
}