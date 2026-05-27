var RE = {};
RE.editor = document.getElementById("editor");

RE.notify = function() {
  if (window.AndroidEditor && window.AndroidEditor.onChange) {
    window.AndroidEditor.onChange(RE.getHtml());
  }
};

RE.setHtml = function(contents) {
  RE.editor.innerHTML = contents || "";
};

RE.getHtml = function() {
  return RE.editor.innerHTML;
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
