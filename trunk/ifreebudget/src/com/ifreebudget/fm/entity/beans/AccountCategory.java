package com.ifreebudget.fm.entity.beans;

/**
 * @author iFreeBudget ifreebudget@gmail.com
 * 
 */
public final class AccountCategory implements FManEntity,
        Comparable<AccountCategory>, Cloneable {
    private static final long serialVersionUID = 1L;
    Long categoryId;
    Long parentCategoryId;
    String categoryName;

    public AccountCategory() {
        categoryName = "";
    }

    public AccountCategory(Long id, Long pid) {
        this.categoryId = id;
        this.parentCategoryId = pid;
        categoryName = "default";
    }

    public Long getCategoryId() {
        return categoryId;
    }

    public void setCategoryId(Long categoryId) {
        this.categoryId = categoryId;
    }

    public Long getParentCategoryId() {
        return parentCategoryId;
    }

    public void setParentCategoryId(Long parentCategoryId) {
        this.parentCategoryId = parentCategoryId;
    }

    public String getCategoryName() {
        return categoryName;
    }

    public void setCategoryName(String categoryName) {
        this.categoryName = categoryName;
    }

    public String toString() {
        StringBuffer ret = new StringBuffer();
        // if (Messages.hasTranslation(categoryName)) {
        // ret.append(Messages.tr(categoryName));
        // }
        // else {
        // ret.append(categoryName);
        // }
        ret.append(categoryName);
        return ret.toString();
    }

    public int hashCode() {
        int result = 17;
        result = 37 * result + categoryId.hashCode();
        result = 37 * result + categoryName.hashCode();
        return result;
    }

    public boolean equals(Object that) {
        if (this == that)
            return true;
        if (!(that instanceof AccountCategory)) {
            return false;
        }
        AccountCategory tmp = (AccountCategory) that;
        boolean ret = categoryId.equals(tmp.getCategoryId());
        return ret;
    }

    public int compareTo(AccountCategory o) {
        if (this == o)
            return 0;
        AccountCategory c = (AccountCategory) o;
        int ret = categoryId.compareTo(c.getCategoryId());
        return ret;
    }

    public Object clone() throws CloneNotSupportedException {
        AccountCategory obj = (AccountCategory) super.clone();

        return obj;
    }

    // Interface methods

    public String getPKColumnName() {
        return "categoryId";
    }

    public Object getPK() {
        return getCategoryId();
    }

    public void setPK(Object pk) {
        setCategoryId((Long) pk);
    }

    public TableMapper getTableMapper() {
        return new AccountCategoryMapper();
    }
}