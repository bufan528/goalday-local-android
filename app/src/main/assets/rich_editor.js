var RE = {};
RE.editor = document.getElementById("editor");

RE.sanitize = function(contents) {
  var template = document.createElement("template");
  template.innerHTML = contents || "";
  var blocked = template.content.querySelectorAll("script,style,iframe,object,embed,link,meta");
  blocked.forEach(function(node) {
    node.remove();
  });
  template.content.querySelectorAll("*").forEach(function(node) {
    Array.prototype.slice.call(node.attributes).forEach(function(attr) {
      var name = attr.name.toLowerCase();
      var value = (attr.value || "").trim().toLowerCase();
      if (name.indexOf("on") === 0 || ((name === "href" || name === "src") && value.indexOf("javascript:") === 0)) {
        node.removeAttribute(attr.name);
      }
    });
  });
  return template.innerHTML;
};

RE.notify = function() {
  if (window.AndroidEditor && window.AndroidEditor.onChange) {
    window.AndroidEditor.onChange(RE.getHtml());
  }
};

RE.setHtml = function(contents) {
  RE.editor.innerHTML = RE.sanitize(contents);
};

RE.getHtml = function() {
  return RE.sanitize(RE.editor.innerHTML);
};

RE.setPlaceholder = function(placeholder) {
  RE.editor.setAttribute("placeholder", placeholder);
};

RE.focus = function() {
  RE.editor.focus();
};

RE.command = function(name, value) {
  document.execCommand(name, false, value || null);
  RE.notify();
  RE.focus();
};

RE.editor.addEventListener("input", function() {
  RE.notify();
});
