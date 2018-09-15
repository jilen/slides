# 杭州 Meetup 资料分享

+ cats-effect-and-fs2.html 为生成的PPT，可以直接浏览器中查看
+ cats-effect-and-fs2.pdf 为生成的PDF，可以直接打开
+ cats-effect-and-fs2.org 为源文件

## 从源文件编译

+ 安装[graphviz](https://www.graphviz.org)(部分绘图需要graphviz)
+ 安装[plantuml](http://plantuml.com)(同为绘图需求)
+ 安装配置[org-reveal](https://github.com/yjwen/org-reveal)
```lisp
(use-package ox-reveal
  :config
  (setq org-reveal-title-slide nil)
  (use-package graphviz-dot-mode
    :config
    (org-babel-do-load-languages
     'org-babel-load-languages
     '((dot . t)))
    (setq org-plantuml-jar-path (expand-file-name "~/.local/share/plantuml/plantuml.jar"))
    (org-babel-do-load-languages
     'org-babel-load-languages
     '((plantuml . t)))))
 ```
+ C-c C-e R R 生成html
