package uob.oop;


public class HtmlParser {
    /***
     * Extract the title of the news from the _htmlCode.
     * @param _htmlCode Contains the full HTML string from a specific news. E.g. 01.htm.
     * @return Return the title if it's been found. Otherwise, return "Title not found!".
     */
    public static String getNewsTitle(String _htmlCode) {
        String titleTagOpen = "<title>";
        String titleTagClose = "</title>";

        int titleStart = _htmlCode.indexOf(titleTagOpen) + titleTagOpen.length();
        int titleEnd = _htmlCode.indexOf(titleTagClose);

        if (titleStart != -1 && titleEnd != -1 && titleEnd > titleStart) {
            String strFullTitle = _htmlCode.substring(titleStart, titleEnd);
            return strFullTitle.substring(0, strFullTitle.indexOf(" |"));
        }

        return "Title not found!";
    }

    /***
     * Extract the content of the news from the _htmlCode.
     * @param _htmlCode Contains the full HTML string from a specific news. E.g. 01.htm.
     * @return Return the content if it's been found. Otherwise, return "Content not found!".
     */
    public static String getNewsContent(String _htmlCode) {
        String contentTagOpen = "\"articleBody\": \"";
        String contentTagClose = " \",\"mainEntityOfPage\":";

        int contentStart = _htmlCode.indexOf(contentTagOpen) + contentTagOpen.length();
        int contentEnd = _htmlCode.indexOf(contentTagClose);

        if (contentStart != -1 && contentEnd != -1 && contentEnd > contentStart) {
            return _htmlCode.substring(contentStart, contentEnd).toLowerCase();
        }

        return "Content not found!";
    }

    public static NewsArticles.DataType getDataType(String _htmlCode) {
        //TODO Task 3.1 - 1.5 Marks
        String dataTagOpen = "<datatype>";
        String dataTagClose = "</datatype>";
        int dataTypeStart = _htmlCode.indexOf(dataTagOpen);
        int dataTypeEnd = _htmlCode.indexOf(dataTagClose, dataTypeStart);
        //System.out.println("start: " + dataTypeStart);
        //System.out.println("end: " + dataTypeEnd);

        if (dataTypeStart != -1 && dataTypeEnd != -1) {
            String dataType = _htmlCode.substring(dataTypeStart + dataTagOpen.length(), dataTypeEnd).toLowerCase();
            return dataType.equals("training") ? NewsArticles.DataType.Training: NewsArticles.DataType.Testing;
        }
        return NewsArticles.DataType.Testing; //Please modify the return value.
    }

    public static String getLabel (String _htmlCode) {
        //TODO Task 3.2 - 1.5 Marks
        String labelTagOpen = "<label>";
        String labelTagClose = "</label>";
        int labelStart = _htmlCode.indexOf(labelTagOpen);
        int labelEnd = _htmlCode.indexOf(labelTagClose, labelStart);
        //System.out.println("start: " + labelStart);
        //System.out.println("end: " + labelEnd);

        if (labelStart != -1 && labelEnd != -1) {
            return _htmlCode.substring(labelStart+labelTagOpen.length(), labelEnd);
        }
        return "-1"; //Please modify the return value.
    }
}
