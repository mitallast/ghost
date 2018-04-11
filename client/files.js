var dropContainer = document.querySelector('.files-drop-container')

dropContainer.addEventListener("dragover", function(e){
    e.stopPropagation();
    e.preventDefault();
    console.log("dragover", e)
}, false)

dropContainer.addEventListener("dragleave", function(e){
    e.stopPropagation();
    e.preventDefault();
    console.log("dragleave", e)
}, false)

dropContainer.addEventListener("drop", function(e){
    e.stopPropagation();
    e.preventDefault();
    console.log("drop", e)

    var files = e.target.files || e.dataTransfer.files;

    // process all File objects
    for (var i = 0, f; f = files[i]; i++) {
        console.log("file", f)

        var img = document.querySelector(".files-drop-image > img")
        img.src = window.URL.createObjectURL(f)
        img.onload = function() {
            window.URL.revokeObjectURL(img.src);
        }

        var reader = new FileReader()
        reader.onabort = function(){console.log("file onabort")}
        reader.onerror = function(){console.log("file onerror")}
        reader.onload = function(){console.log("file onload", reader.result)}
        reader.onloadstart = function(){console.log("file onloadstart")}
        reader.onloadend = function(){console.log("file onloadend")}
        reader.onprogress = function(){console.log("file onprogress")}
        reader.readAsArrayBuffer(f)
    }
}, false)
