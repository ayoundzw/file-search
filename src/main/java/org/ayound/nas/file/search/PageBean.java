package org.ayound.nas.file.search;

import java.util.ArrayList;
import java.util.List;

public class PageBean
{

    private int currentPage = 1;// 当前页数
    private int totalPages = 0;// 总页数
    private int pageSize = 0;// 每页显示数
    private int totalRows = 0;// 总数据数
    private int startNum = 0;// 开始记录
    private int nextPage = 0;// 下一页
    private int previousPage = 0;// 上一页
    private boolean hasNextPage = false;// 是否有下一页
    private boolean hasPreviousPage = false;// 是否有前一页
    private List<String> pageCodes;
    private int showPageSize = 8; //显示多少个超链接页面
    
    public PageBean() {}

    public PageBean(int pageSize, int currentPage, int totalRows)
    {

        this.pageSize = pageSize;
        this.currentPage = currentPage;
        this.totalRows = totalRows;

        if ((totalRows % pageSize) == 0)
        {
            totalPages = totalRows / pageSize;
        } else
        {
            totalPages = totalRows / pageSize + 1;
        }

        if (currentPage >= totalPages)
        {
            hasNextPage = false;
            currentPage = totalPages;
        } else
        {
            hasNextPage = true;
        }

        if (currentPage <= 1)
        {
            hasPreviousPage = false;
            currentPage = 1;
        } else
        {
            hasPreviousPage = true;
        }

        startNum = (currentPage - 1) * pageSize;
        nextPage = currentPage + 1;

        if (nextPage >= totalPages)
        {
            nextPage = totalPages;
        }

        previousPage = currentPage - 1;

        if (previousPage <= 1)
        {
            previousPage = 1;
        }

        reflashPageCode();
    }
    
    public void reflashPageCode()
    {
        this.pageCodes = new ArrayList<String>();
        if (this.totalPages <= this.showPageSize)
        {
            for (int i = 1; i <= this.totalPages; i++)
            {
                this.pageCodes.add(String.valueOf(i));
            }
            return;
        }
        int middleSide = this.showPageSize >> 1;
        if (this.currentPage <= middleSide)
        {
            for (int i = 1; i <= this.showPageSize; i++)
            {
                this.pageCodes.add(String.valueOf(i));
            }
            this.pageCodes.add(String.valueOf(".."));
            return;
        }
        if ((this.totalPages - this.currentPage) <= middleSide)
        {
            this.pageCodes.add(String.valueOf(".."));
            for (int i = this.showPageSize - 1; i >= 0; i--)
            {
                this.pageCodes.add(String.valueOf(this.totalPages - i));
            }
            return;
        }
        if (middleSide < this.currentPage
                && this.currentPage - (middleSide + 1) > 0)
            this.pageCodes.add(String.valueOf(".."));

        for (int i = 0; i < this.showPageSize; i++)
        {
            this.pageCodes.add(String.valueOf((this.currentPage + i)
                    - middleSide));
        }
        if (middleSide > this.currentPage
                || this.totalPages - (this.currentPage + middleSide) > 0)
            this.pageCodes.add(String.valueOf(".."));
    }

    public boolean isHasNextPage()
    {
        return hasNextPage;
    }

    public boolean isHasPreviousPage()
    {
        return hasPreviousPage;
    }

    /**
     * @return the nextPage
     */
    public int getNextPage()
    {
        return nextPage;
    }

    /**
     * @param nextPage
     *            the nextPage to set
     */
    public void setNextPage(int nextPage)
    {
        this.nextPage = nextPage;
    }

    /**
     * @return the previousPage
     */
    public int getPreviousPage()
    {
        return previousPage;
    }

    /**
     * @param previousPage
     *            the previousPage to set
     */
    public void setPreviousPage(int previousPage)
    {
        this.previousPage = previousPage;
    }

    /**
     * @return the currentPage
     */
    public int getCurrentPage()
    {
        return currentPage;
    }

    /**
     * @param currentPage
     *            the currentPage to set
     */
    public void setCurrentPage(int currentPage)
    {
        this.currentPage = currentPage;
    }

    /**
     * @return the pageSize
     */
    public int getPageSize()
    {
        return pageSize;
    }

    /**
     * @param pageSize
     *            the pageSize to set
     */
    public void setPageSize(int pageSize)
    {
        this.pageSize = pageSize;
    }

    /**
     * @return the totalPages
     */
    public int getTotalPages()
    {
        return totalPages;
    }

    /**
     * @param totalPages
     *            the totalPages to set
     */
    public void setTotalPages(int totalPages)
    {
        this.totalPages = totalPages;
    }

    /**
     * @return the totalRows
     */
    public int getTotalRows()
    {
        return totalRows;
    }

    /**
     * @param totalRows
     *            the totalRows to set
     */
    public void setTotalRows(int totalRows)
    {
        this.totalRows = totalRows;
    }

    /**
     * @param hasNextPage
     *            the hasNextPage to set
     */
    public void setHasNextPage(boolean hasNextPage)
    {
        this.hasNextPage = hasNextPage;
    }

    /**
     * @param hasPreviousPage
     *            the hasPreviousPage to set
     */
    public void setHasPreviousPage(boolean hasPreviousPage)
    {
        this.hasPreviousPage = hasPreviousPage;
    }

    /**
     * @return the startNum
     */
    public int getStartNum()
    {
        return startNum;
    }

    /**
     * @param startNum
     *            the startNum to set
     */
    public void setStartNum(int startNum)
    {
        this.startNum = startNum;
    }

    public List<String> getPageCodes()
    {
        if (this.pageCodes == null) {
              return new ArrayList<String>();
            }
        return pageCodes;
    }

    public void setPageCodes(List<String> pageCodes)
    {
        this.pageCodes = pageCodes;
    }

    public int getShowPageSize()
    {
        return showPageSize;
    }

    public void setShowPageSize(int showPageSize)
    {
        this.showPageSize = showPageSize;
    }
    
}