        // <![CDATA[

        var jckdata_builds = document.getElementById('jckdata_builds').textContent.split(/\s*,\s*/).flatMap((s) => (s.trim()));
        var jckdata_errors = document.getElementById('jckdata_errors').textContent.split(/\s*,\s*/).flatMap((s) => (s.trim()));
        var jckdata_failed = document.getElementById('jckdata_failed').textContent.split(/\s*,\s*/).flatMap((s) => (s.trim()));
        var jckdata_total = document.getElementById('jckdata_total').textContent.split(/\s*,\s*/).flatMap((s) => (s.trim()));
        var jckdata_runned = document.getElementById('jckdata_runned').textContent.split(/\s*,\s*/).flatMap((s) => (s.trim()));
        var jckdata_imps = document.getElementById('jckdata_imps').textContent.split(/\s*,\s*/).flatMap((s) => (s.trim()));
        var jckdata_regs = document.getElementById('jckdata_regs').textContent.split(/\s*,\s*/).flatMap((s) => (s.trim()));

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

