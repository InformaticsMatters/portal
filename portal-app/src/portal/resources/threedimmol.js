
var glviewer = null;
var labels = [];

var addLabels = function() {
    var atoms = glviewer.getModel().selectedAtoms({
        atom : "CA"
    });
    for ( var a in atoms) {
        var atom = atoms[a];

        var l = glviewer.addLabel(atom.resn + " " + atom.resi, {
            inFront : true,
            fontSize : 12,
            position : {
                x : atom.x,
                y : atom.y,
                z : atom.z
            }
        });
        atom.label = l;
        labels.push(atom);
    }
};

var colorSS = function(viewer) {
    //color by secondary structure
    var m = viewer.getModel();
    m.setColorByFunction({}, function(atom) {
        if(atom.ss == 'h') return "magenta";
        else if(atom.ss == 's') return "orange";
        else return "white";
    });
    viewer.render();
}

var atomcallback = function(atom, viewer) {
    if (atom.clickLabel === undefined
            || !atom.clickLabel instanceof $3Dmol.Label) {
        atom.clickLabel = viewer.addLabel(atom.elem + atom.serial, {
            fontSize : 14,
            position : {
                x : atom.x,
                y : atom.y,
                z : atom.z
            },
            backgroundColor: "black"
        });
        atom.clicked = true;
    }

    //toggle label style
    else {

        if (atom.clicked) {
            var newstyle = atom.clickLabel.getStyle();
            newstyle.backgroundColor = 0x66ccff;

            viewer.setLabelStyle(atom.clickLabel, newstyle);
            atom.clicked = !atom.clicked;
        }
        else {
            viewer.removeLabel(atom.clickLabel);
            delete atom.clickLabel;
            atom.clicked = false;
        }

    }
};

var readText = function(input, func) {
    if(input.files.length > 0) {
        var file = input.files[0];
        var reader = new FileReader();
        reader.onload = function(evt) {
            func(evt.target.result, file.name);
        };
        reader.readAsText(file);
        $(input).val('');
    }
};

/* usage of file upload

<input type="file" onchange="readText(this, function(data, name) {glviewer.clear(); m=glviewer.addModel(data, name); glviewer.zoomTo(); glviewer.render();} );">

*/

function init3DMolViewer(data) {
    glviewer = $3Dmol.createViewer("gldiv", {defaultcolors : $3Dmol.rasmolElementColors});
    glviewer.setBackgroundColor(0xffffff);

    glviewer.addModel(data, "sdf");
    // $3Dmol.download('pdb:1UBQ', glviewer, {doAssembly:true, noSecondaryStructure: false});
    glviewer.setStyle({}, {stick: {}});

    glviewer.mapAtomProperties($3Dmol.applyPartialCharges);

    glviewer.zoomTo();
    glviewer.render();
}
