package com.bazinga.base;

/**
 * @author yunshan
 * @date 2018/12/20
 */
public class Sort {
    private static final long serialVersionUID = -3992572843995793776L;
    private String column;
    private Sort.SortType type;

    public Sort(String column, Sort.SortType type) {
        this.column = column;
        this.type = type;
    }

    public String getColumn() {
        return this.column;
    }

    public Sort.SortType getType() {
        return this.type;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        } else if (!(o instanceof Sort)) {
            return false;
        } else {
            Sort sort = (Sort)o;
            if (this.column != null) {
                if (this.column.equals(sort.column)) {
                    return this.type == sort.type;
                }
            } else if (sort.column == null) {
                return this.type == sort.type;
            }

            return false;
        }
    }

    @Override
    public int hashCode() {
        int result = this.column != null ? this.column.hashCode() : 0;
        result = 31 * result + (this.type != null ? this.type.hashCode() : 0);
        return result;
    }

    @Override
    public String toString() {
        return this.column + ' ' + this.type.getMessage();
    }

    public static enum SortType {
        ASC(0, "ASC"),
        DESC(1, "DESC");

        private int value;
        private String message = null;

        private SortType(int value, String message) {
            this.value = value;
            this.message = message;
        }

        public int getValue() {
            return this.value;
        }

        public String getMessage() {
            return this.message;
        }

        public static Sort.SortType getByValue(int value) {
            Sort.SortType[] var1 = values();
            int var2 = var1.length;

            for(int var3 = 0; var3 < var2; ++var3) {
                Sort.SortType sortTypeEnum = var1[var3];
                if (sortTypeEnum.getValue() == value) {
                    return sortTypeEnum;
                }
            }

            return null;
        }

        @Override
        public String toString() {
            return this.value + "|" + this.message;
        }
    }
}
