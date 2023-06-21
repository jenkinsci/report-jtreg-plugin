        // <![CDATA[

        var jckdata_builds_element = document.getElementById('jckdata_builds');
        if (jckdata_builds_element == null) {
                var jckdata_builds = ["missing labels"]
        } else {
                var jckdata_builds = jckdata_builds_element.textContent.split(/\s*,\s*/).flatMap((s) => (s.trim()));
        }

        var jckdata_errors_element = document.getElementById('jckdata_errors');
        if (jckdata_errors_element == null) {
                var jckdata_errors = ["0"]
        } else {
                var jckdata_errors = jckdata_errors_element.textContent.split(/\s*,\s*/).flatMap((s) => (s.trim()));
        }

        var jckdata_failed_element = document.getElementById('jckdata_failed');
        if (jckdata_failed_element == null) {
                var jckdata_failed = ["0"]
        } else {
                var jckdata_failed = jckdata_failed_element.textContent.split(/\s*,\s*/).flatMap((s) => (s.trim()));
        }

        var jckdata_total_element = document.getElementById('jckdata_total');
        if (jckdata_total_element == null) {
                var jckdata_total = ["0"]
        } else {
                var jckdata_total = jckdata_total_element.textContent.split(/\s*,\s*/).flatMap((s) => (s.trim()));
        }

        var jckdata_runned_element = document.getElementById('jckdata_runned');
        if (jckdata_runned_element == null) {
                var jckdata_runned = ["0"]
        } else {
                var jckdata_runned = jckdata_runned_element.textContent.split(/\s*,\s*/).flatMap((s) => (s.trim()));
        }

        var jckdata_imps_element = document.getElementById('jckdata_imps');
        if (jckdata_imps_element == null) {
                var jckdata_imps = ["0"]
        } else {
                var jckdata_imps = jckdata_imps_element.textContent.split(/\s*,\s*/).flatMap((s) => (s.trim()));
        }

        var jckdata_regs_element = document.getElementById('jckdata_regs');
        if (jckdata_regs_element == null) {
                var jckdata_regs = ["0"]
        } else {
                var jckdata_regs = jckdata_regs_element.textContent.split(/\s*,\s*/).flatMap((s) => (s.trim()));
        }


        var allJckFails = {
          type: 'line',
          data: {
            labels: jckdata_builds,
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
                pointRadius: 5,
                        data: jckdata_errors
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
                pointRadius: 5,
                        data: jckdata_failed
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
                var activePoints = jckErrorsChart.getElementsAtEventForMode(e, 'index', { intersect: false }, true);
                var point = activePoints[0]
                var datasetIndex = point.datasetIndex //labels are for all data together,  no need to look into exact dataset
                var index = point.index
                var result = jckErrorsChart.config.data.labels[index]
                var buildId = result.substring(result.lastIndexOf(":") + 1)
                window.open("" + buildId + "/java-reports", "_blank");
          }
         }
        };
        var ctx = document.getElementById("jckErrorsFailuresChart").getContext("2d");
        var jckErrorsChart = new Chart(ctx, allJckFails);

        var allJck = {
          type: 'line',
          data: {
            labels: jckdata_builds,
                datasets: [
                {
                label: "Tests total",
                fill: true,
                backgroundColor: "rgba(180,180,180,0.2)",
                borderColor: "rgba(180,180,180,1)",
                pointBackgroundColor: "rgba(180,180,180,1)",
                pointBorderColor: "#fff",
                pointHoverBackgroundColor: "#fff",
                pointHoverBorderColor: "rgba(180,180,180,1)",
                pointRadius: 5,
                        data: jckdata_total
                },
                {
                label: "Runned tests",
                fill: true,
                backgroundColor: "rgba(180,180,180,0.2)",
                borderColor: "rgba(180,180,180,1)",
                pointBackgroundColor: "rgba(180,180,180,1)",
                pointBorderColor: "#fff",
                pointHoverBackgroundColor: "#fff",
                pointHoverBorderColor: "rgba(180,180,180,1)",
                pointRadius: 5,
                        data: jckdata_runned
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
                var activePoints = jckPassedChartTck.getElementsAtEventForMode(e, 'index', { intersect: false }, true);
                var point = activePoints[0]
                var datasetIndex = point.datasetIndex //labels are for all data together,  no need to look into exact dataset
                var index = point.index
                var result = jckPassedChartTck.config.data.labels[index]
                var buildId = result.substring(result.lastIndexOf(":") + 1)
                window.open("" + buildId + "/java-reports", "_blank");
            }
         }
        };
        var ctx = document.getElementById("jckPassedChart").getContext("2d");
        var jckPassedChartTck = new Chart(ctx, allJck);

        var allJckRegressions = {
          type: 'bar',
          data: {
            labels: jckdata_builds,
                datasets: [
                {
                label: "Improvements",
                backgroundColor: "rgba(0,220,0,0.5)",
                borderColor: "rgba(0,220,0,0.8)",
                borderWidth: 2,
                barThickness: 20,
                hoverBackgroundColor: "rgba(0,220,0,0.75)",
                hoverBorderColor: "rgba(0,220,0,1)",
                        data: jckdata_imps
                },
                {
                label: "Regressions",
                backgroundColor: "rgba(220,0,0,0.5)",
                borderColor: "rgba(220,0,0,0.8)",
                borderWidth: 2,
                barThickness: 20,
                hoverBackgroundColor: "rgba(220,0,0,0.75)",
                hoverBorderColor: "rgba(220,0,0,1)",
                        data: jckdata_regs
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
                var activePoints = jckRegressions.getElementsAtEventForMode(e, 'index', { intersect: false }, true);
                var point = activePoints[0]
                var datasetIndex = point.datasetIndex //labels are for all data together,  no need to look into exact dataset
                var index = point.index
                var result = jckRegressions.config.data.labels[index]
                var buildId = result.substring(result.lastIndexOf(":") + 1)
                window.open("" + buildId + "/java-reports", "_blank");
           }
         }
        };
        var ctx = document.getElementById("jckRegressionsChart").getContext("2d");
        var jckRegressions = new Chart(ctx, allJckRegressions);

        //hopefully temporary workaround to weird sizes
        var jc1 = document.getElementById('jckErrorsFailuresChartContainer')
        jc1.style.width = "599px";
        jc1.style.height = "599px";
        var jc2 = document.getElementById('jckPassedChartContainer')
        jc2.style.width = "599px";
        jc2.style.height = "599px";
        var jc3 = document.getElementById('jckRegressionsChartContainer')
        jc3.style.width = "599px";
        jc3.style.height = "599px";
        // ]]>

