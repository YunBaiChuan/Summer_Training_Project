package com.gxa.test;

import org.junit.jupiter.api.Test;
import org.springframework.ai.reader.ExtractedTextFormatter;
import org.springframework.ai.reader.pdf.PagePdfDocumentReader;
import org.springframework.ai.reader.pdf.config.PdfDocumentReaderConfig;
import org.springframework.ai.document.Document;
import org.springframework.ai.embedding.EmbeddingModel;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.core.io.FileSystemResource;

import java.util.List;

@SpringBootTest
public class testLoadPdf {

    // 注入向量存储（根据您的配置，应该是Pinecone）
    @Autowired
    private VectorStore vectorStore;

    // 可选：注入Embedding模型
    @Autowired(required = false)
    private EmbeddingModel embeddingModel;

    @Test
    public void testLoadPdf() {
        try {
            // 1. 加载资源
            FileSystemResource resource = new FileSystemResource(
                    "book.pdf"
            );

            System.out.println("PDF文件路径: " + resource.getFile().getAbsolutePath());
            System.out.println("文件存在: " + resource.exists());

            // 2. 创建PDF读取器
            PagePdfDocumentReader reader = new PagePdfDocumentReader(
                    resource,
                    PdfDocumentReaderConfig.builder()
                            .withPageExtractedTextFormatter(ExtractedTextFormatter.defaults())
                            .withPagesPerDocument(1)  // 每页作为一个document
                            .build()
            );

            // 3. 读取pdf文件，拆分成document
            List<Document> documentList = reader.read();  // 使用read()方法

            System.out.println("成功读取PDF，文档数量: " + documentList.size());

            // 4. 打印前3个文档的内容
            for (int i = 0; i < Math.min(3, documentList.size()); i++) {
                Document doc = documentList.get(i);
                System.out.println("\n=== 文档 " + (i + 1) + " ===");

                // 方法1：尝试getText()
                try {
                    String text = doc.getText();
                    System.out.println("内容(getText): " +
                            (text.length() > 100 ? text.substring(0, 100) + "..." : text));
                } catch (Exception e) {
                    System.out.println("getText()方法不存在");
                }

                // 方法2：尝试toString()
                System.out.println("文档对象: " + doc.toString());

                // 方法3：查看元数据
                try {
                    Object metadata = doc.getMetadata();
                    System.out.println("元数据: " + metadata);
                } catch (Exception e) {
                    System.out.println("getMetadata()方法不存在");
                }
            }

            // 5. 存储数据到向量数据库
            if (vectorStore != null) {
                vectorStore.add(documentList);
                System.out.println("\n成功将 " + documentList.size() + " 个文档存储到向量数据库");
            } else {
                System.out.println("\n警告: vectorStore为null，无法存储到向量数据库");
                System.out.println("请检查VectorStore的配置，您配置的是Pinecone向量存储");
            }

        } catch (Exception e) {
            System.err.println("加载PDF失败: " + e.getMessage());
            e.printStackTrace();
        }
    }
}