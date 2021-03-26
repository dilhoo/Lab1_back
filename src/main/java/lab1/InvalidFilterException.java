package lab1;

public class InvalidFilterException extends Exception {

    private final String filteredColumn;
    private final Object invalidFilterValue;

    public InvalidFilterException(String filteredColumn, Object invalidFilterValue) {
        super("Invalid '" + filteredColumn + "' filter : " + invalidFilterValue);

        this.filteredColumn = filteredColumn;
        this.invalidFilterValue = invalidFilterValue;
    }

    public String getFilteredColumn() {
        return filteredColumn;
    }

    public Object getInvalidFilterValue() {
        return invalidFilterValue;
    }
}
