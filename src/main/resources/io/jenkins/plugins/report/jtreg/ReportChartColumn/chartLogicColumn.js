                // <![CDATA[
function readJcks() {
    var allJckChartIdElements = document.getElementsByClassName("jckChart-names")
    for (let i = 0; i < allJckChartIdElements.length; i++) {
        var keyAttribute = allJckChartIdElements[i].getAttribute("jckChart_processed")
        if (keyAttribute == null) {
            continue
        }
        if (keyAttribute != "false") {
            continue
        }
        allJckChartIdElements[i].setAttribute("jckChart_processed", "true")
        var id = allJckChartIdElements[i].textContent.trim();
        var data_builds = document.getElementById('jckChart-buildNumber-'+id).textContent.split(/\s*,\s*/).flatMap((s) => (s.trim()));
        var data_failed = document.getElementById('jckChart-failed-'+id).textContent.split(/\s*,\s*/).flatMap((s) =>(s.trim()));
        var data_error = document.getElementById('jckChart-error-'+id).textContent.split(/\s*,\s*/).flatMap((s) =>(s.trim()));
        var data_url = document.getElementById('jckChart-url-'+id).textContent.trim();

                if (typeof jckChartNameVar == 'undefined') {
                  var jckChartNameVar = {};
                }
                var dataJckView = {
                    labels: data_builds,
                            datasets: [
                            {
                            label: "Error",
                            fillColor: "rgba(255,0,255,0.2)",
                            strokeColor: "rgba(255,0,255,1)",
                            pointColor: "rgba(255,0,255,1)",
                            pointStrokeColor: "#fff",
                            pointHighlightFill: "#fff",
                            pointHighlightStroke: "rgba(255,0,255,1)",
                                    data: data_error
                            },
                            {
                            label: "Failed",
                            fillColor: "rgba(255,0,0,0.2)",
                            strokeColor: "rgba(255,0,0,1)",
                            pointColor: "rgba(255,0,0,1)",
                            pointStrokeColor: "#fff",
                            pointHighlightFill: "#fff",
                            pointHighlightStroke: "rgba(255,0,0,1)",
                                    data: data_failed
                            }
                            ]
                    };
                    var options = {
                        url_from_job: data_url,
                        bezierCurve: false,
                        multiTooltipTemplate: "<%= datasetLabel + \": \" + value %>"
                    }
                    var ctx = document.getElementById("jckChart-"+id).getContext("2d");
                    jckChartNameVar[id]  = new Chart(ctx).Line(dataJckView, options);
                    document.getElementById("jckChartContainer-"+id).onclick = function (evt) {
                            var lid = event.target.id;
                            var jid = lid.replace("jckChart-", "")
                            var chart = jckChartNameVar[jid]
                            var activePoints = chart.getPointsAtEvent(evt);
                            var point = activePoints[0]
                            var datasetIndex = point.datasetIndex //labels are for all data together,  no need to look into exact dataset
                            var index = point.index
                            var result = point.label
                            var buildId = result.substring(result.lastIndexOf(":") + 1) //thsi works even without :; fixme, add NVR?
                            window.open("/"+chart.options.url_from_job+buildId+"/java-reports", "_blank");
                        };

    }
}
window.addEventListener('load', readJcks, false);
// ]]>
