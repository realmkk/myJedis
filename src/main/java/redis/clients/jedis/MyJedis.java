package redis.clients.jedis;

import redis.clients.jedis.util.ArrayUtils;
import redis.clients.jedis.util.Utils;

import java.io.IOException;
import java.io.InputStream;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class MyJedis {
    private static final String CRLF = "\r\n";
    private Socket socket = null;

    public MyJedis() {
        getSocket("localhost", 6380, "redis");
    }

    public MyJedis(String ip, int port) {
        getSocket(ip, port);
    }

    public MyJedis(String ip, int port, String password) {
        getSocket(ip, port, password);
    }

    /**
     * 通过url连接redis
     * 格式为ip:port[/password]
     * @param url
     */
    public MyJedis(String url) {
        String addr, password, ip;
        int port;
        if (url.indexOf("/") > 0) {
            String[] splitUrl = url.split("/");
            addr = splitUrl[0];
            password = splitUrl[1];
            ip = addr.split(":")[0];
            port = Integer.parseInt(addr.split(":")[1]);
            getSocket(ip, port, password);
        } else {
            addr = url;
            ip = addr.split(":")[0];
            port = Integer.parseInt(addr.split(":")[1]);
            getSocket(ip, port);
        }
    }

    private void getSocket(String ip, int port) {
        try {
            socket = new Socket(ip, port);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void getSocket(String ip, int port, String password) {
        try {
            socket = new Socket(ip, port);
            auth(password);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public String auth(final String password) {
        return (String) execute("auth", password);
    }

    public String set(final String key, final String value) {
        return (String) execute("set", key, value);
    }

    public String get(final String key) {
        return (String) execute("get", key);
    }

    public Long del(final String key) {
        return (Long) execute("del", key);
    }

    public Long hset(final String key, String field, String value) {
        return (Long) execute("hset", key, field, value);
    }

    public String hget(final String key, String field) {
        return (String) execute("hget", key, field);
    }

    public String hmset(final String key, String... fieldAndValue) {
        fieldAndValue = ArrayUtils.addStringToHead(key, fieldAndValue);
        fieldAndValue = ArrayUtils.addStringToHead("hmset", fieldAndValue);
        return (String) execute(fieldAndValue);
    }

    public List<String> hmget(final String key, String... field) {
        return (List<String>) execute(ArrayUtils.addStringsToHead(field, "hmget", key));
    }

    public Map<String, String> hgetall(final String key) {
        List<String> list = (List<String>) execute("hgetall", key);
        return Utils.parseMap(list);
    }

    public Stream xrange(String key, String start, String end) {
        List<List<Object>> list = (List<List<Object>>) execute("xrange", key, start, end);
        Stream stream = new Stream();
        list.forEach(x -> {
            StreamEntry streamEntry = new StreamEntry(x.get(0).toString(), Utils.parseMap((List<String>) x.get(1)));
            stream.add(streamEntry);
        });
        return stream;
    }

    public Stream xrange(String key, String start, String end, int count) {
        Stream stream = new Stream();
        if (count < 1) return stream;

        List<List<Object>> list = (List<List<Object>>) execute("xrange", key, start, end, "COUNT", String.valueOf(count));

        list.forEach(x -> {
            StreamEntry streamEntry = new StreamEntry(x.get(0).toString(), Utils.parseMap((List<String>) x.get(1)));
            stream.add(streamEntry);
        });
        return stream;
    }

    public Object execute(String... args) {
        System.out.println("Command: " + ArrayUtils.of(args));
        try {
            String command = getCommandString(args);
            socket.getOutputStream().write(command.getBytes());
            InputStream in = socket.getInputStream();

            return getResult(in);
        } catch (IOException e) {
            return null;
        }
    }

    /**
     * 执行字符串形式的redis命令
     * @param command
     * @return
     */
    public Object executeClientCommand(String command) {
        List<String> list = ArrayUtils.of(command.split(" "));
        List<String> commandList = new ArrayList<>();
        boolean half = false;
        StringBuilder sb = new StringBuilder();
        for (String x : list) {
            if (!half) {
                if ("".equals(x)) {
                    //处理连续多个空格的情况
                    continue;
                }
                if ("\"".equals(x)) {
                    //如果引号后带空格，split之后会产生单个引号
                    sb.append(x);
                    half = true;
                    continue;
                }
                if (x.startsWith("\"")) {
                    if (x.endsWith("\"") && !x.endsWith("\\\"")) {
                        //end
                        commandList.add(x);
                    } else {
                        //noEnd
                        sb.append(x);
                        half = true;
                    }
                } else {
                    commandList.add(x);
                }
            } else {
                sb.append(" ").append(x);
                if (x.endsWith("\"") && !x.endsWith("\\\"")) {
                    //end
                    sb.deleteCharAt(0).deleteCharAt(sb.length()-1);
                    commandList.add(sb.toString());
                    sb.setLength(0);
                    half = false;
                }
            }
        }
//        System.out.println(commandList);
        return execute(commandList.toArray(new String[commandList.size()]));
    }

    private String getCommandString(String... args) {
        StringBuilder sb = new StringBuilder();
        sb.append("*").append(args.length).append(CRLF);

        ArrayUtils.of(args).forEach(x -> {
            if (x == null) throw new RuntimeException("value sent to redis cannot be null");
            sb.append("$").append(x.getBytes().length).append(CRLF)
                    .append(x).append(CRLF);
        });
        return sb.toString();
    }

    @Deprecated
    private Object getResult(String resultStr) {
//        System.out.println("result: " + resultStr);
        String[] result = resultStr.split(CRLF);
        int offset = 0;
        if (result[offset].startsWith("+")) {
            //+OK
            return result[offset].substring(1);
        } else if (result[offset].startsWith(":")) {
            //:1
            return Long.parseLong(result[offset].substring(1));
        } else if (result[offset].startsWith("$")) {
            //string
            int start = resultStr.indexOf(CRLF);
            return resultStr.substring(start);
        } else if (result[offset].startsWith("*")) {
            //list
            List<String> list = new ArrayList<>();
            for (int i = offset + 2; i <= result.length; i += 2) {
                if (i == result.length) list.add(null);
                else list.add(result[i]);
            }
            return list;
        } else if (result[offset].startsWith("-")) {
            //error
            throw new RuntimeException(resultStr);
        }
        return null;
    }

    public Object getResult(InputStream in) throws IOException {
        char type = (char) in.read();
        switch (type) {
            //响应码
            case '+': {
                int count = in.available();
                byte[] b = new byte[count];
                in.read(b, 0, count);
                return new String(b);
            }
            //字符串
            case '$': {
                StringBuffer sbLen = new StringBuffer();

                char b;
                while (true) {
                    b = (char) in.read();
                    if (b == '\r') {
                        in.read();
                        break;
                    }
                    sbLen.append(b);
                }
                int len = Integer.parseInt(sbLen.toString());

                if (len == -1) return null;
                if (len == 0) {
                    in.skip(2);
                    return "";
                }

                StringBuffer sb = new StringBuffer();

                while (in.available() < len) {}
                byte[] con = new byte[len];
                in.read(con, 0, len);
                in.skip(2);
                sb.append(new String(con));
                return sb.toString();
            }
            //异常
            case '-': {
                int count = in.available();
                byte[] b = new byte[count];
                in.read(b, 0, count);
                throw new RuntimeException(new String(b));
            }
            //数字
            case ':': {
                int count = in.available();
                byte[] b = new byte[count-2];
                in.read(b, 0, count-2);
                in.skip(in.available());
                return Long.parseLong(new String(b));
            }
            //多笔
            case '*': {
                StringBuffer sbLen = new StringBuffer();

                char c;
                while (true) {
                    c = (char) in.read();
                    if (c == '\r') {
                        in.read();
                        break;
                    }
                    sbLen.append(c);
                }
                int len = Integer.parseInt(sbLen.toString());
                List<Object> result = new ArrayList<>();
                for (int i = 0; i < len; i++) {
                    result.add(getResult(in));
                }
                return result;
            }
            default:
                throw new RuntimeException("Unknown reply message start with: " + type);
        }
    }
}
