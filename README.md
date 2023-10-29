# MeteoMaster

![GitHub last commit](https://img.shields.io/github/last-commit/thehackermonk/meteo-master?style=flat-square) ![GitHub](https://img.shields.io/github/license/thehackermonk/meteo-master?style=flat-square) ![GitHub issues](https://img.shields.io/github/issues/thehackermonk/meteo-master?style=flat-square) ![GitHub language count](https://img.shields.io/github/languages/count/thehackermonk/meteo-master?style=flat-square) ![GitHub top language](https://img.shields.io/github/languages/top/thehackermonk/meteo-master?logo=java&style=flat-square) ![GitHub code size in bytes](https://img.shields.io/github/languages/code-size/thehackermonk/meteo-master?style=flat-square) ![Twitter Follow](https://img.shields.io/twitter/follow/thehackermonk?style=flat-square)

MeteoMaster is a feature-rich web application designed to provide users with accurate and up-to-date weather information for locations around the world. With a user-friendly interface and a wide range of features, MeteoMaster is your go-to source for weather data. Whether you're planning your day, week, or just curious about the current weather, this app has you covered.

## Features

### Now Menu

- **Current Weather**: Get the current weather conditions for your selected location.
- **High and Low Temperatures**: See the highest and lowest temperatures of the day.
- **Timezone**: Know the timezone of the selected location.
- **Sunrise and Sunset Time**: Find out when the sun will rise and set.
- **Current Rainfall**: Check the current rainfall in the area.
- **Snowfall**: Get updates on the current snowfall, if any.
- **UV Index**: Stay informed about the UV index.
- **Wind Information**: Access details about the current wind conditions.
- **Visibility**: Know the visibility in the area.

### Day Menu

- **Current Temperature**: Discover the current temperature for the selected day.
- **Rainfall and Snowfall**: Find out how much rain and snow to expect.
- **UV Index**: Learn about the UV index for the day.
- **Wind Information**: Get details about the wind conditions.
- **Visibility**: See the visibility in the area.
- **Graphical Data**: View data for the entire day in a graph format when you select a specific parameter.

### Week Menu

- **Date**: See the date for the next seven days.
- **High and Low Temperatures**: Get the highest and lowest temperatures for each day.
- **Sunrise and Sunset Times**: Find out when the sun will rise and set for each day.
- **UV Index**: Stay updated on the UV index for the upcoming week.

### Location Selection

- Choose from a list of 78 cities and towns across the globe to get accurate weather data.

### Location Change Limit

- Users are not allowed to change their selected location on the same day, ensuring efficient use of resources.

### Alerts

- MeteoMaster displays weather-related alerts for the next two hours, helping you stay informed about short-term weather conditions.

## Getting Started Locally

To run MeteoMaster on your local machine, follow these steps:

1. Clone the repository:

   ```bash
   git clone https://github.com/yourusername/your-repo-name
2. Navigate to the project folder:
   ```bash
   cd your-repo-name
3. Install the necessary dependencies for the front-end and back-end. Refer to the respective sections.
4. Start both the front-end and back-end components.
5. Open a web browser and access the app at http://localhost:your-port (replace your-port with the port you've configured).
6. Explore the "Now," "Day," and "Week" menus to access detailed weather information.
7. Enjoy up-to-date and accurate weather data to plan your activities and stay informed.

## Front-End
The front-end of MeteoMaster is developed using ReactJS.
### UI Dependencies
* [axios](https://www.npmjs.com/package/axios)
* [bootstrap](https://www.npmjs.com/package/bootstrap)
* [chart.js](https://www.npmjs.com/package/chart.js)
* [react-chartjs-2](https://www.npmjs.com/package/react-chartjs-2)
* [weather-icons-react](https://www.npmjs.com/package/weather-icons-react)

## Back-End
The back-end of MeteoMaster is developed using Spring Boot.
Build and run the Spring Boot application using your preferred Java development tools.
## License

MeteoMaster is available under the [MIT License](LICENSE.md). You are free to use, modify, and distribute the code, subject to the terms of the license.

## Author

- Karthik Prakash

## Screenshots
![Current Weather View](https://github.com/thehackermonk/meteo-master/blob/main/screenshots/NowView.png)
![Day View](https://github.com/thehackermonk/meteo-master/blob/main/screenshots/DayView.png)
![Week View](https://github.com/thehackermonk/meteo-master/blob/main/screenshots/WeekView.png)
If the images doesn't render please go to the screenshots folder
