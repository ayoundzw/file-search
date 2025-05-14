package org.ayound.nas.file.search;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.tokenattributes.CharTermAttribute;
import org.apache.lucene.document.Document;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.Term;
import org.apache.lucene.search.BooleanClause;
import org.apache.lucene.search.BooleanQuery;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.MatchAllDocsQuery;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.search.Sort;
import org.apache.lucene.search.SortField;
import org.apache.lucene.search.TermQuery;
import org.apache.lucene.search.TopDocs;
import org.apache.lucene.search.WildcardQuery;
import org.apache.lucene.search.BooleanClause.Occur;
import org.apache.lucene.search.SortField.Type;
import org.apache.lucene.search.highlight.Highlighter;
import org.apache.lucene.search.highlight.QueryScorer;
import org.apache.lucene.search.highlight.SimpleFragmenter;
import org.apache.lucene.search.highlight.SimpleHTMLFormatter;
import org.apache.lucene.store.Directory;
import org.json.JSONArray;
import org.json.JSONObject;

public class Searcher {
    private static Logger log = LogManager.getLogger(Searcher.class);
    private static volatile Searcher instance;

    private Searcher() throws IOException {
    }

    public static Searcher getInstance() throws IOException {
        if (instance == null) {
            synchronized(Searcher.class) {
                if (instance == null) {
                    instance = new Searcher();
                }
            }
        }

        return instance;
    }

    boolean indexed(String path, long time) throws IOException {
        Query pathQuery = new TermQuery(new Term("path", path));
        Directory indexDirectory = IndexService.getInstance().getIndexDirectory();
        if (indexDirectory.listAll().length <= 1) {
            return false;
        } else {
            IndexSearcher searcher = new IndexSearcher(DirectoryReader.open(indexDirectory));
            TopDocs docs = searcher.search(pathQuery, 10);
            if (docs.scoreDocs.length > 0) {
                ScoreDoc doc = docs.scoreDocs[0];
                Document document = searcher.doc(doc.doc);
                String storedTime = document.get("time");
                if (StringUtils.equals(storedTime, String.valueOf(time))) {
                    return true;
                }
            }

            return false;
        }
    }

    private static List<String> doToken(TokenStream ts) throws IOException {
        List<String> strs = new ArrayList();
        ts.reset();
        CharTermAttribute cta = (CharTermAttribute)ts.getAttribute(CharTermAttribute.class);

        while(ts.incrementToken()) {
            String str = cta.toString();
            if (str.length() > 1) {
                strs.add(str);
            }
        }

        System.out.println();
        ts.end();
        ts.close();
        return strs;
    }

    public JSONObject search(String type, String text, int page, int pageSize, String sortField, boolean direct) throws IOException {
        JSONObject ret = new JSONObject();
        Sort sort = new Sort(new SortField[]{new SortField(sortField, Type.LONG, direct)});

        try {
            List<String> keys = new ArrayList();
            JSONArray array = new JSONArray();
            ret.put("data", array);
            Query query = null;
            Object var29;
            if (StringUtils.trim(text).length() == 0) {
                var29 = new MatchAllDocsQuery();
            } else if ("f".equals(type)) {
                String[] arrs = text.split(" ");
                BooleanQuery.Builder builder = new BooleanQuery.Builder();
                if (arrs.length <= 0) {
                    var29 = new MatchAllDocsQuery();
                } else {
                    for(String str : arrs) {
                        if (str.length() > 0) {
                            BooleanClause.Occur occur = Occur.MUST;
                            if (str.startsWith("-")) {
                                occur = Occur.MUST_NOT;
                                str = str.replaceFirst("-", "");
                            } else {
                                keys.add(str);
                            }

                            str = "*" + str + "*";
                            Term term = new Term("name", str);
                            WildcardQuery termQuery = new WildcardQuery(term);
                            String fieldName = term.field();
                            log.info("需要查询字段的标题（查询结果只包含文件名）：Field Name: " + fieldName + "，检索词str为" + str);
                            builder.add(termQuery, occur);
                        }
                    }

                    var29 = builder.build();
                }
            } else if ("f2".equals(type)) {
                String[] arrs = text.split(" ");
                BooleanQuery.Builder builder = new BooleanQuery.Builder();
                if (arrs.length <= 0) {
                    var29 = new MatchAllDocsQuery();
                } else {
                    for(String str : arrs) {
                        if (str.length() > 0) {
                            BooleanClause.Occur occur = Occur.MUST;
                            if (str.startsWith("-")) {
                                occur = Occur.MUST_NOT;
                                str = str.replaceFirst("-", "");
                            } else {
                                keys.add(str);
                            }

                            str = "*" + str + "*";
                            WildcardQuery termQuery = new WildcardQuery(new Term("path", str));
                            builder.add(termQuery, occur);
                        }
                    }

                    var29 = builder.build();
                }
            } else if ("f3".equals(type)) {
                String[] arrs = text.split(" ");
                BooleanQuery.Builder builder = new BooleanQuery.Builder();
                if (arrs.length <= 0) {
                    var29 = new MatchAllDocsQuery();
                } else {
                    for(String str : arrs) {
                        if (str.length() > 0) {
                            BooleanClause.Occur occur = Occur.MUST;
                            if (str.startsWith("-")) {
                                occur = Occur.MUST_NOT;
                                str = str.replaceFirst("-", "");
                            } else {
                                keys.add(str);
                            }

                            str = "*" + str + "*";
                            Term term = new Term("path", str);
                            WildcardQuery termQuery = new WildcardQuery(term);
                            String fieldName = term.field();
                            log.info("需要查询字段的标题（查询结果也包含路径）：Field Name: " + fieldName + "，检索词str为" + str);
                            builder.add(termQuery, occur);
                        }
                    }

                    var29 = builder.build();
                }
                
            }else if ("f1".equals(type)) {
                TokenStream stream = IndexService.getInstance().getAnalyzer().tokenStream("path", text);
                List<String> arrs = doToken(stream);
                if (arrs.size() <= 0) {
                    var29 = new MatchAllDocsQuery();
                } else {
                    BooleanQuery.Builder builder = new BooleanQuery.Builder();

                    for(String str : arrs) {
                        if (str.length() > 0) {
                            keys.add(str);
                            str = "*" + str + "*";
                            WildcardQuery termQuery = new WildcardQuery(new Term("path", str));
                            builder.add(termQuery, Occur.SHOULD);
                        }
                    }

                    var29 = builder.build();
                }
            } else if ("c".equals(type)) {
                String[] arrs = text.split(" ");
                if (arrs.length <= 0) {
                    var29 = new MatchAllDocsQuery();
                } else {
                    BooleanQuery.Builder builder = new BooleanQuery.Builder();

                    for(String str : arrs) {
                        if (str.length() > 0) {
                            BooleanClause.Occur occur = Occur.MUST;
                            if (str.startsWith("-")) {
                                occur = Occur.MUST_NOT;
                                str = str.replaceFirst("-", "");
                            } else {
                                keys.add(str);
                            }

                            TermQuery termQuery = new TermQuery(new Term("content", str));
                            builder.add(termQuery, occur);
                        }
                    }

                    var29 = builder.build();
                }
            } else if (!"c1".equals(type)) {
                var29 = new MatchAllDocsQuery();
            } else {
                TokenStream stream = IndexService.getInstance().getAnalyzer().tokenStream("content", text);
                List<String> arrs = doToken(stream);
                if (arrs.size() <= 0) {
                    var29 = new MatchAllDocsQuery();
                } else {
                    BooleanQuery.Builder builder = new BooleanQuery.Builder();

                    for(String str : arrs) {
                        if (str.length() > 0) {
                            keys.add(str);
                            TermQuery termQuery = new TermQuery(new Term("content", str));
                            builder.add(termQuery, Occur.SHOULD);
                        }
                    }

                    var29 = builder.build();
                }
            }

            IndexSearcher searcher = new IndexSearcher(DirectoryReader.open(IndexService.getInstance().getIndexDirectory()));
            int count = searcher.count((Query)var29);
            ret.put("count", count);
            ret.put("page", page);
            ScoreDoc[] hits = new ScoreDoc[0];
            if (page <= 1) {
                hits = searcher.search((Query)var29, pageSize, sort).scoreDocs;
            } else {
                int allSize = page * pageSize;
                ScoreDoc[] allDocs = searcher.search((Query)var29, allSize, sort).scoreDocs;
                List<ScoreDoc> list = new ArrayList();
                int start = (page - 1) * pageSize;
                if (start < allDocs.length) {
                    for(int i = start; i < allDocs.length; ++i) {
                        list.add(allDocs[i]);
                    }
                }

                hits = (ScoreDoc[])list.toArray(new ScoreDoc[0]);
            }

            QueryScorer scorer = new QueryScorer((Query)var29);
            SimpleHTMLFormatter simpleHtmlFormatter = new SimpleHTMLFormatter("<B><font color='red'>", "</font></B>");
            Highlighter highlighter = new Highlighter(simpleHtmlFormatter, scorer);
            highlighter.setTextFragmenter(new SimpleFragmenter(200));
            Analyzer analyzer = IndexService.getInstance().getAnalyzer();

            for(ScoreDoc hit : hits) {
                Document document = searcher.doc(hit.doc);
                JSONObject obj = new JSONObject();
                array.put(obj);
                obj.put("name", document.get("name"));
                obj.put("path", document.get("path"));
                obj.put("time", document.get("time"));
                obj.put("size", document.get("size"));
                if (type.startsWith("c")) {
                    String content = document.get("content");
                    String str = "";
                    if (content != null) {
                        str = highlighter.getBestFragment(analyzer, "content", content);
                    }

                    obj.put("match", str);
                }

                obj.put("doc", hit.doc);
                obj.put("shardIndex", hit.shardIndex);
            }

            ret.put("keys", keys);
            ret.put("result", "ok");
        } catch (Exception pe) {
            String info = "Failed to parse query: " + pe.getMessage();
            pe.printStackTrace();
            ret.put("result", "error");
            ret.put("msg", info);
        }

        return ret;
    }

    public Document getPath(int id) {
        try {
            IndexSearcher searcher = new IndexSearcher(DirectoryReader.open(IndexService.getInstance().getIndexDirectory()));
            Document document = searcher.doc(id);
            return document;
        } catch (IOException e) {
            e.printStackTrace();
            String info = "Failed to parse query: " + e.getMessage();
            e.printStackTrace();
            return null;
        }
    }

    public String get(int id) {
        JSONObject ret = new JSONObject();

        try {
            IndexSearcher searcher = new IndexSearcher(DirectoryReader.open(IndexService.getInstance().getIndexDirectory()));
            Document document = searcher.doc(id);
            JSONObject obj = new JSONObject();
            obj.put("name", document.get("name"));
            obj.put("path", document.get("path"));
            obj.put("time", document.get("time"));
            obj.put("size", document.get("size"));
            String content = document.get("content");
            if (content != null && content.length() > 10000) {
                content = StringUtils.substring(content, 0, 10000);
            }

            obj.put("content", content);
            obj.put("type", document.get("type"));
            ret.put("data", obj);
            ret.put("result", "ok");
        } catch (IOException e) {
            e.printStackTrace();
            String info = "Failed to parse query: " + e.getMessage();
            e.printStackTrace();
            ret.put("result", "error");
            ret.put("msg", info);
        }

        return ret.toString();
    }
}
