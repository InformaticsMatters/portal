var get_random_data	= function() {
		var data = [];

		var getRandomInt = function(min, max) {
		  return Math.floor(Math.random() * (max - min + 1)) + min;
		}

		for(var i = 0; i < 10000; i++) {
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

function execute() {
		var filter	= crossfilter(get_random_data());

		var data = {
			a: { dimension: filter.dimension(function(d){ return d.a; }) },
			b: { dimension: filter.dimension(function(d){ return d.b; }) },
			c: { dimension: filter.dimension(function(d){ return d.c; }) },
			d: { dimension: filter.dimension(function(d){ return d.d; }) }
		};

		data.a.group = data.a.dimension.group(function(d){ return Math.floor(d / 1)*1; });
		data.b.group = data.b.dimension.group(function(d){ return Math.floor(d / 10)*10; });
		data.c.group = data.c.dimension.group(function(d){ return Math.floor(d / 100)*100; });
		data.d.group = data.d.dimension.group(function(d){ return Math.floor(d / 1000)*1000; });

		var a = ay_histogram('histogram-a', data.a, {margin: [10, 10], bin_width: 1, tick_width: 75});
		var b = ay_histogram('histogram-b', data.b, {margin: [10, 10], bin_width: 10, tick_width: 75});
		var c = ay_histogram('histogram-c', data.c, {margin: [10, 10], bin_width: 100, tick_width: 75});
		var d = ay_histogram('histogram-d', data.d, {margin: [10, 10], bin_width: 1000, tick_width: 75});

		a.setRelations([b,c,d]);
		b.setRelations([a,c,d]);
		c.setRelations([a,b,d]);
		d.setRelations([a,b,c]);
}