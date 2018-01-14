package me.lingfengsan.hero.keyword;

import java.io.Serializable;
import java.util.List;

/**
 * Created by maxliaops on 18-1-14.
 */

public class KeywordsResponse implements Serializable {
    private String id;
    private ResultEntity result;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public ResultEntity getResult() {
        return result;
    }

    public void setResult(ResultEntity result) {
        this.result = result;
    }

    public static class ResultEntity {

        private int _ret;
        private ResEntity res;

        public int get_ret() {
            return _ret;
        }

        public void set_ret(int _ret) {
            this._ret = _ret;
        }

        public ResEntity getRes() {
            return res;
        }

        public void setRes(ResEntity res) {
            this.res = res;
        }

        public static class ResEntity {
            /**
             * keyword_list : ["春秋战国","名医","时期"]
             * keyword_type_list : ["single","single","single"]
             * real_title : 春秋战国时期名医
             * title : 春秋战国时期名医
             * url :
             * wordpos : ["春秋战国:n","时期:n","名医:n"]
             * wordrank : ["春秋战国:3:0.516171","时期:2:0.207056","名医:2:0.276772"]
             */

            private String real_title;
            private String title;
            private String url;
            private List<String> keyword_list;
            private List<String> keyword_type_list;
            private List<String> wordpos;
            private List<String> wordrank;

            public String getReal_title() {
                return real_title;
            }

            public void setReal_title(String real_title) {
                this.real_title = real_title;
            }

            public String getTitle() {
                return title;
            }

            public void setTitle(String title) {
                this.title = title;
            }

            public String getUrl() {
                return url;
            }

            public void setUrl(String url) {
                this.url = url;
            }

            public List<String> getKeyword_list() {
                return keyword_list;
            }

            public void setKeyword_list(List<String> keyword_list) {
                this.keyword_list = keyword_list;
            }

            public List<String> getKeyword_type_list() {
                return keyword_type_list;
            }

            public void setKeyword_type_list(List<String> keyword_type_list) {
                this.keyword_type_list = keyword_type_list;
            }

            public List<String> getWordpos() {
                return wordpos;
            }

            public void setWordpos(List<String> wordpos) {
                this.wordpos = wordpos;
            }

            public List<String> getWordrank() {
                return wordrank;
            }

            public void setWordrank(List<String> wordrank) {
                this.wordrank = wordrank;
            }
        }
    }
}
