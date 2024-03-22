package org.ayound.nas.file.search;

import java.util.List;

import org.apache.lucene.document.Document;

public class SearchResultBean
{
    private int totalHits;
    private List<Document> docs;

    public SearchResultBean()
    {
        
    }

    public SearchResultBean(int totalHits, List<Document> docs)
    {
        this.totalHits = totalHits;
        this.docs = docs;
    }

    public int getTotalHits()
    {
        return this.totalHits;
    }

    public void setTotalHits(int totalHits)
    {
        this.totalHits = totalHits;
    }

    public List<Document> getDocs()
    {
        return this.docs;
    }

    public void setDocs(List<Document> docs)
    {
        this.docs = docs;
    }
}