###suggest的用法(用于自动补全)

刘德 --- 刘德华


#####搜索自动补全(CompletionSuggestionBuilder)


``` lua
SearchRequestBuilder req = client.prepareSearch(index);
req.setQuery(QueryBuilders.matchAllQuery());
CompletionSuggestionBuilder csfb = new CompletionSuggestionBuilder("sug").field(field).text(value).size(100);//sug是提示器的名字，可以有多个suggest
req.addSuggestion(csfb);
SearchResponse suggestResponse = req.execute().actionGet();
List<? extends Suggest.Suggestion.Entry<? extends Suggest.Suggestion.Entry.Option>> results = suggestResponse.getSuggest().getSuggestion("sug").getEntries();
System.out.println(JSON.toJSONString(suggestResponse));
for (Suggest.Suggestion.Entry<? extends Suggest.Suggestion.Entry.Option> op : results) {
    Text text = op.getText();
    System.out.println(text.toString());
    List<? extends Suggest.Suggestion.Entry.Option> options = op.getOptions();
    for (Suggest.Suggestion.Entry.Option pp : options) {
        System.out.println("\t" + pp.getText() + "==>" + pp.getScore());
    }
}
    
```

#####错别词纠正(TermSuggestionBuilder)

``` lua
SearchResponse response = client.prepareSearch("wikipedia")
	.setQuery(QueryBuilders.matchAllQuery())
	.addSuggestion(new TermSuggestionBuilder("first_suggestion")
	.text("graphics designer")
	.field("_all")).execute().actionGet();
 for( Entry<? extends Option> entry : response.getSuggest().getSuggestion("first_suggestion").getEntries()) {
 System.out.println("Check for: " + entry.getText() + ". Options:")
 for( Option option : entry.getOptions()){
	System.out.println("\t" + option.getText());
 }
}
```

#####短语错别纠正(PhraseSuggestionBuilder)
     
   - 如果想对短语进行提示的胡可以使用PhraseSuggestionBuilder来做，
   PhraseSuggestionBuilder在TermSuggestionBuilder的基础上添加了额外的短语计算逻辑，从而可以返回完整的短语检疫而不是单个的词项建议。用法和TermSuggestionBuilder一样。