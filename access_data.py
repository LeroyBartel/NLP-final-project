import json
import requests


def main():

    app_id = 0
    review_offset = 0
    data = []
    while app_id < 100:

        print(app_id)

        if not isValidAppID(app_id):
            app_id += 1
            continue

        while review_offset < 1000:
            # day range 9223372036854775807 might be a hack
            url = 'http://store.steampowered.com/appreviews/' + str(app_id) + '?json=1' + \
                  '&filter=all' + \
                  '&language=all' + \
                  '&day_range=9223372036854775807' + \
                  '&start_offset=' + str(review_offset) + \
                  '&review_type=all' + \
                  '&purchase_type=all' + \
                  '&num_per_page=100'
            print(url)
            req = requests.get(url)
            data.append(req.json())
            review_offset += 100

        review_offset = 0
        app_id += 1

    with open('review_data.json', 'w', encoding='utf-8') as out_file:
        json.dump(data, out_file, indent=4)


def isValidAppID(ID):
    url = 'http://store.steampowered.com/appreviews/' + str(ID) + '?json=1'
    data = requests.get(url).json()
    return data["success"] == 1


if __name__ == '__main__':
    main()