package com.deko.mall.system.utils;

import cn.hutool.core.util.XmlUtil;
import org.glassfish.jaxb.core.marshaller.CharacterEscapeHandler;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import java.io.IOException;
import java.io.StringReader;
import java.io.StringWriter;
import java.io.Writer;
import java.util.HashMap;
import java.util.Map;

public class XmlUtils extends XmlUtil {

    public static void main(String[] args) {
        String xmlStr = "<?xml version=\"1.0\" encoding=\"utf-8\"?>\n" +
                "<SubmitResult xmlns=\"http://106.veesing.com/\">\n" +
                "<code>2</code>\n" +
                "<msg>提交成功</msg>\n" +
                "<smsid>16632288727325440151</smsid>\n" +
                "</SubmitResult>\n";
        Map map = parseMap(xmlStr);
        System.out.println(1);
    }

    /**
     * xml字符串转为bean
     *
     * @param xml   xml字符串
     * @param clazz 需要转成的对象
     */
    @SuppressWarnings("unchecked")
    public static <T> T parseBean(String xml, Class<T> clazz) {
        try {
            JAXBContext context = JAXBContext.newInstance(clazz);
            Unmarshaller unmarshaller = context.createUnmarshaller();
            return (T) unmarshaller.unmarshal(new StringReader(xml));
        } catch (JAXBException e) {
            e.printStackTrace();
            return null;
        }
    }

    public static Map<String, String> parseMap(String xmlStr) {
        Map<String, String> map = new HashMap<String, String>();
        if (xmlStr == null) {
            return map;
        }
        try {
            StringReader sr = new StringReader(xmlStr);
            InputSource is = new InputSource(sr);
            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            DocumentBuilder builder = factory.newDocumentBuilder();
            Document doc = builder.parse(is);
            Element ele = doc.getDocumentElement();
            NodeList nodes = ele.getChildNodes();
            if (nodes != null) {
                int nodesLen = nodes.getLength();
                for (int i = 0; i < nodesLen; i++) {
                    Node node = nodes.item(i);
                    String k = node.getNodeName();
                    String v = node.getTextContent();
                    map.put(k, v);
                }
            }
        } catch (Exception e) {
        }
        return map;
    }

    /**
     * 将对象转为xml字符串
     * 编码为utf-8
     * 不格式化
     * 省略头部声明
     *
     * @param obj 待转的对象
     */
    public static String toStr(Object obj) {
        return toStr(obj, false, true);
    }

    /**
     * 将对象转为xml字符串
     * 编码为utf-8
     *
     * @param obj          待转的对象
     * @param isFormat     是否格式化
     * @param isIgnoreHead 是否忽略头部
     */
    public static String toStr(Object obj, boolean isFormat, boolean isIgnoreHead) {
        try {
            JAXBContext context = JAXBContext.newInstance(obj.getClass());
            Marshaller marshaller = context.createMarshaller();
            marshaller.setProperty(Marshaller.JAXB_ENCODING, "UTF-8");// //编码格式
            marshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, isFormat);// 是否格式化生成的xml串
            marshaller.setProperty(Marshaller.JAXB_FRAGMENT, isIgnoreHead);// 是否省略xm头声明信息

            // 不进行转义字符的处理
            marshaller.setProperty(CharacterEscapeHandler.class.getName(),
                    new CharacterEscapeHandler() {
                        @Override
                        public void escape(char[] ch, int start, int length,
                                           boolean isAttVal, Writer writer) throws IOException {
                            writer.write(ch, start, length);
                        }
                    });

            StringWriter writer = new StringWriter();
            marshaller.marshal(obj, writer);
            return writer.toString();
        } catch (JAXBException e) {
            e.printStackTrace();
            return null;
        }
    }

    /**
     * 将对象转为xml字符串
     * 编码为utf-8
     * 格式化
     * 省略头部声明
     *
     * @param obj 待转的对象
     */
    public static String toPrettyStr(Object obj) {
        return toStr(obj, true, true);
    }


    /**
     * 将对象转为xml字符串
     * 编码为 GBK
     *
     * @param obj          待转的对象
     * @param isFormat     是否格式化
     * @param isIgnoreHead 是否忽略头部
     */
    public static String toStrGF(Object obj, boolean isFormat, boolean isIgnoreHead) {
        try {
            JAXBContext context = JAXBContext.newInstance(obj.getClass());
            Marshaller marshaller = context.createMarshaller();
            marshaller.setProperty(Marshaller.JAXB_ENCODING, "GBK");// //编码格式
            marshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, isFormat);// 是否格式化生成的xml串
            marshaller.setProperty(Marshaller.JAXB_FRAGMENT, isIgnoreHead);// 是否省略xm头声明信息

            // 不进行转义字符的处理
            marshaller.setProperty(CharacterEscapeHandler.class.getName(),
                    new CharacterEscapeHandler() {
                        @Override
                        public void escape(char[] ch, int start, int length,
                                           boolean isAttVal, Writer writer) throws IOException {
                            writer.write(ch, start, length);
                        }
                    });

            StringWriter writer = new StringWriter();
            marshaller.marshal(obj, writer);
            return writer.toString();
        } catch (JAXBException e) {
            e.printStackTrace();
            return null;
        }
    }
}
