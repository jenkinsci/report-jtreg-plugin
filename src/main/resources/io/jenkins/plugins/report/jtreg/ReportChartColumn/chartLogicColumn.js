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
                  type: 'line',
                  url_from_job: data_url,
                  data:  {
                    labels: data_builds,
                            datasets: [
                            {
                            label: "Error",
                            fill: true,
                            backgroundColor: "rgba(255,0,255,0.2)",
                            borderColor: "rgba(255,0,255,1)",
                            pointBackgroundColor: "rgba(255,0,255,1)",
                            pointBorderColor: "#fff",
                            pointHoverBackgroundColor: "#fff",
                            pointHoverBorderColor: "rgba(255,0,255,1)",
                            pointRadius: 4,
                                    data: data_error
                            },
                            {
                            label: "Failed",
                            fill: true,
                            backgroundColor: "rgba(255,0,0,0.2)",
                            borderColor: "rgba(255,0,0,1)",
                            pointBackgroundColor: "rgba(255,0,0,1)",
                            pointBorderColor: "#fff",
                            pointHoverBackgroundColor: "#fff",
                            pointHoverBorderColor: "rgba(255,0,0,1)",
                            pointRadius: 4,
                                    data: data_failed
                            }
                            ]
                    },
                    options: {
                        plugins: {
                            legend: { display: false }
                        },
                        interaction: {
                            mode: 'index',
                            intersect: false
                        },
                        onClick: (e) => {
                            var chart = e.chart;
                            var activePoints = chart.getElementsAtEventForMode(e, 'index', { intersect: false }, true);
                            var point = activePoints[0]
                            var datasetIndex = point.datasetIndex //labels are for all data together,  no need to look into exact dataset
                            var index = point.index
                            var result = chart.config.data.labels[index]
                            var buildId = result.substring(result.lastIndexOf(":") + 1) //thsi works even without :; fixme, add NVR?
                            window.open("/"+chart.config._config.url_from_job+buildId+"/java-reports", "_blank");
                    }
                    }
                };
                    var ctx = document.getElementById("jckChart-"+id).getContext("2d");
                    jckChartNameVar[id]  = new Chart(ctx, dataJckView);

    }
}
window.addEventListener('load', readJcks, false);
// ]]>
