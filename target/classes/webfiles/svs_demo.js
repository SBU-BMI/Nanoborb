var prefixurl = "https://cdn.jsdelivr.net/npm/openseadragon@2.3/build/openseadragon/images/";
var url=window.location.search;
console.log("calling URL : "+url);
var urlParams = new URLSearchParams(window.location.search);
var p = urlParams.get('id');
console.log("image ID : "+p);
fetch(p+"/info.json").then(response => {
    return response.json();
}).then(data => {
    var viewer1 = OpenSeadragon({
    id: "openseadragon1",
    prefixUrl: prefixurl,
	defaultZoomLevel: 1.0,
        tileSources: {
            "@context": "http://iiif.io/api/image/2/context.json",
            //"@id": "/iiif?iri=https://s3.amazonaws.com/ebremeribox/TCGA-02-0001-01C-01-BS1.0cc8ca55-d024-440c-a4f0-01cf5b3af861.svs",
            "@id": p,
            "height": data.height,
            "width": data.width,
            "profile": [ "http://iiif.io/api/image/2/level2.json" ],
            "protocol": "http://iiif.io/api/image",
            "tiles": [{
              "scaleFactors": [ 1, 2, 4, 8, 16, 32],
              "width": 256
            }]
        }
    });
}).catch(err => {
    console.log("ack! something unexpected happened.");
});

