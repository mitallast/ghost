
var root = document.getElementById('scroll');
var viewport = root.querySelector(':scope > .scroll-viewport')
var barContainer = root.querySelector(':scope > .scroll-bar-container')
var scrollbar = barContainer.querySelector(':scope > .scroll-bar')

var scrollbar = {
    init: function() {
        root.addEventListener('wheel',scrollbar.wheel, {passive: false})
        scrollbar.addEventListener('mousedown', scrollbar.mousedown)
        var ro = new ResizeObserver(scrollbar.resize);
        ro.observe(root)
        ro.observe(viewport)
    },
    resize: function(){
        var h = Math.floor(barContainer.offsetHeight * (viewport.offsetHeight / viewport.scrollHeight));
        var offset = Math.floor(barContainer.offsetHeight * (viewport.scrollTop / viewport.scrollHeight));

        if(h ==  barContainer.offsetHeight) {
            scrollbar.style.display = 'none';
        }else{
            scrollbar.style.height = h + "px";
            scrollbar.style.display = 'block';
            scrollbar.style.top = offset + "px";
        }
    },
    scrollDelta: function(delta) {
        var maxOffset = viewport.scrollHeight - viewport.offsetHeight;
        var newOffset = Math.max(0, Math.min(maxOffset, viewport.scrollTop + delta))
        viewport.scrollTop = newOffset;
        scrollbar.resize();
    },
    wheel: function(e){
        scrollbar.scrollDelta(e.deltaY / 10)
    },
    mouseY: 0,
    mousedown: function(e){
        e.preventDefault();
        document.addEventListener('mouseup', scrollbar.mouseup);
        document.addEventListener('mousemove', scrollbar.mousemove);
        scrollbar.mouseY = e.clientY;
    },
    mouseup: function(e){
        document.removeEventListener('mouseup', scrollbar.resize);
        document.removeEventListener('mousemove', scrollbar.mousemove);
    },
    mousemove: function(e){
        var delta = e.clientY - scrollbar.mouseY
        scrollbar.mouseY = e.clientY;
        scrollbar.scrollDelta(delta);
    },
}

scrollbar.init();