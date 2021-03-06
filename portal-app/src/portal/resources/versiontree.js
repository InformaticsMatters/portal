function createTree(data) {
    if (data == "") {
        return;
    }

    var margin = {top: 20, right: 120, bottom: 20, left: 180};
    var width = 960 - margin.right - margin.left;
    var height = 300 - margin.top - margin.bottom;
    var i = 0;
    var duration = 750;

    var tree = d3.layout.tree().size([height, width]);

    var diagonal = d3.svg.diagonal().projection(function(d) { return [d.y, d.x]; });

    $plotContent = $('#versionTreeContainer');
    $plotContent.empty();

    var svg = d3.select($plotContent[0])
        .append("svg")
        .attr("width", width + margin.right + margin.left)
        .attr("height", height + margin.top + margin.bottom)
        .append("g")
        .attr("transform", "translate(" + margin.left + "," + margin.top + ")");

    var root = data;
    root.x0 = height / 2;
    root.y0 = 0;
    if (root.children !== undefined) {
        // root.children.forEach(collapse);
    }
    update(root);

    function collapse(d) {
        if (d.children) {
            d._children = d.children;
            d._children.forEach(collapse);
            d.children = null;
        }
    }

    function update(source) {
        // Compute the new tree layout.
        var nodes = tree.nodes(root).reverse();
        var links = tree.links(nodes);

        // Normalize for fixed-depth.
        nodes.forEach(function(d) { d.y = d.depth * 180; });

        // Update the nodes…
        var node = svg.selectAll("g.node").data(nodes, function(d) { return d.id || (d.id = ++i); });

        // Enter any new nodes at the parent's previous position.
        var nodeEnter = node
            .enter()
            .append("g")
            .attr("class", "node")
            .attr("transform", function(d) { return "translate(" + source.y0 + "," + source.x0 + ")"; })
            .on("click", click)
            .on("dblclick", dblclick);

        nodeEnter.append("circle")
            .attr("r", 1e-6)
            .style("fill", function(d) { return d._children ? "lightsteelblue" : "#fff"; })
            .style("stroke", function(d) {
                if(d.nodeType == "NotebookEditableDTO") return "green";
                if(d.nodeType == "NotebookSavepointDTO") return "orange"; });

        /*
        nodeEnter.append("text")
            .attr("x", function(d) { return d.children || d._children ? -10 : 10; })
            .attr("dy", ".35em")
            .attr("text-anchor", function(d) { return d.children || d._children ? "end" : "start"; })
            .text(function(d) { return d.name; })
            .style("fill-opacity", 1e-6);
            */

        nodeEnter.append("foreignObject")
            .attr("width", 150)
            .attr("height", function(d) {return d.nodeType == "NotebookSavepointDTO" ? 60 : 50})
            .attr("x", function(d) { return d.children || d._children ? -150 : 10; })
            .attr("y", -20)
            .html(function(d) {
                var title = d.nodeType == "NotebookSavepointDTO" ? "<span class='versionCardDescription'>" + d.name + "</span><br>" : "";
                return "" +
                    "<div class='versionTreeCard'>" +
                        title +
                        "<span class='versionCardLabel'>Id: </span><span class='versionCardValue'>" + d.id + "</span>" +
                        "<br><span class='versionCardLabel'>Created: </span><span class='versionCardValue'>" + d.dateCreated + "</span>" +
                        "<br><span class='versionCardLabel'>Updated: </span><span class='versionCardValue'>" + d.dateUpdated + "</span>" +
                    "</div>";
            });

        // Transition nodes to their new position.
        var nodeUpdate = node.transition()
            .duration(duration)
            .attr("transform", function(d) { return "translate(" + d.y + "," + d.x + ")"; });

        nodeUpdate.select("circle")
            .attr("r", 5)
            .style("fill", function(d) { return d._children ? "lightsteelblue" : "#fff"; });

        nodeUpdate.select("text")
            .style("fill-opacity", 1);

        // Transition exiting nodes to the parent's new position.
        var nodeExit = node.exit().transition()
            .duration(duration)
            .attr("transform", function(d) { return "translate(" + source.y + "," + source.x + ")"; })
            .remove();

        nodeExit.select("circle")
            .attr("r", 1e-6);

        nodeExit.select("text")
            .style("fill-opacity", 1e-6);

        // Update the links…
        var link = svg.selectAll("path.link")
            .data(links, function(d) { return d.target.id; });

        // Enter any new links at the parent's previous position.
        link.enter().insert("path", "g")
            .attr("class", "link")
            .attr("d", function(d) {var o = {x: source.x0, y: source.y0}; return diagonal({source: o, target: o});});

        // Transition links to their new position.
        link.transition()
            .duration(duration)
            .attr("d", diagonal);

        // Transition exiting nodes to the parent's new position.
        link.exit().transition()
            .duration(duration)
            .attr("d", function(d) {
                var o = {x: source.x, y: source.y};
                return diagonal({source: o, target: o});
            })
            .remove();

        // Stash the old positions for transition.
        nodes.forEach(function(d) {
            d.x0 = d.x;
            d.y0 = d.y;
        });
    }

    // Toggle children on click.
    function click(d) {
        if (d.children) {
            d._children = d.children;
            d.children = null;
        } else {
            d.children = d._children;
            d._children = null;
        }
        update(d);
    }

    function dblclick(d) {
        onVersionTreeNodeSelection(d.id);
    }

}
