var get_random_data	= function() {
		var data = [];

		var getRandomInt = function(min, max) {
		  return Math.floor(Math.random() * (max - min + 1)) + min;
		}

		for(var i = 0; i < 1000; i++) {
			data.push({
				id: i,
				a: getRandomInt(0,49),
				b: getRandomInt(0,499),
				c: getRandomInt(0,4999),
				d: getRandomInt(0,49999)
			});
		}

		return data;
}

function execute(id, config, data) {

        var fields = config.fields,
            bins = config.bins ? config.bins : 50;

        var outer = d3.select("#"+id);
        outer.selectAll("*").remove();

        var filter	= crossfilter(data);

        var xdata = {};
        var histograms = [];
        for (i=0; i<config.fields.length; i++) {
            var name = fields[i];
            var div = outer.append("div").attr("class", "column");
            div.append("div").attr("class", "column-title").text(name);
            div.append("svg").attr("class", "histogram-" + name);

            xdata[name] = { dimension: filter.dimension(function(d){ return d[name]; }) }

            var min = d3.min(data, function(d) { return d[name] });
            var max = d3.max(data, function(d) { return d[name] });
            var binWidth = Math.round((max - min) / bins);
            console.log("MinMax[" + name + "]: " + min + " " + max + " -> " + binWidth);
            xdata[name].group = xdata[name].dimension.group(function(x){
                var bin = Math.floor(x / binWidth) * binWidth;
                //console.log(x + " -> " + bin);
                return bin;
            });

            histograms.push(ay_histogram('histogram-' + name, xdata[name], {margin: [10, 10], bin_width: binWidth, tick_width: 75}));
        }

        for (i=0; i<histograms.length; i++) {
            var arr = []
            for (j=0; j<histograms.length; j++) {
                if (i != j) {
                    arr.push(histograms[j]);
                }
            }
            histograms[i].setRelations(arr);
        }

}