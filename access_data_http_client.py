import json
import http.client


def main():

    app_id = 0
    app_id_max = 100

    review_offset = 0
    review_limit_per_app = 1000  # multiple of num_per_page
    data = []

    app_list = get_app_list()
    connection = http.client.HTTPSConnection("store.steampowered.com")

    while app_id < app_id_max:

        if not is_valid_app_id(connection, app_id):
            app_id += 1

        else:
            print(get_game_title(app_list, app_id))

            while review_offset < review_limit_per_app:
                # day range 9223372036854775807 might be a hack
                url = "/appreviews/" + str(app_id) + "?json=1" + \
                      "&filter=all" + \
                      "&language=all" + \
                      "&day_range=9223372036854775807" + \
                      "&start_offset=" + str(review_offset) + \
                      "&review_type=all" + \
                      "&purchase_type=all" + \
                      "&num_per_page=100"
                # print(url)
                data.append(get_json(connection, url))
                review_offset += 100

        review_offset = 0
        app_id += 1

    connection.close()

    with open("review_data.json", "w", encoding="utf-8") as out_file:
        json.dump(data, out_file, indent=4)


def is_valid_app_id(connection, ID):
    data = get_json(connection, "/appreviews/" + str(ID) + "?json=1")
    return data["success"] == 1

    
def get_json(connection, url):
    connection.request("GET", url)
    response = connection.getresponse()
    data = response.read()
    return json.loads(data.decode("utf-8"))


def get_app_list():
    connection = http.client.HTTPSConnection("api.steampowered.com")
    connection.request("GET", "/ISteamApps/GetAppList/v2/")
    response = connection.getresponse()
    data = response.read()
    connection.close()
    return json.loads(data.decode("utf-8"))["applist"]["apps"]


def get_game_title(app_list, ID):
    return list(filter(lambda entry: entry['appid'] == ID, app_list))


def get_metadata(json_entry):
    pass


def create_csv_entry(json_entry):
    pass


def create_csv_file(json_data):
    pass

        
if __name__ == "__main__":
    main()
