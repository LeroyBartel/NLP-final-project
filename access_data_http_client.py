import json
import http.client

def main():

    app_id = 0
    review_offset = 0
    data = []
    
    connection = http.client.HTTPSConnection("store.steampowered.com")

    while app_id < 100:

        print(app_id)

        if not isValidAppID(connection, app_id):
            app_id += 1
            continue

        while review_offset < 1000:
            # day range 9223372036854775807 might be a hack
            url = '/appreviews/' + str(app_id) + '?json=1' + \
                  '&filter=all' + \
                  '&language=all' + \
                  '&day_range=9223372036854775807' + \
                  '&start_offset=' + str(review_offset) + \
                  '&review_type=all' + \
                  '&purchase_type=all' + \
                  '&num_per_page=100'
            print(url)
            data.append(get_json(connection, url))
            review_offset += 100
            
        review_offset = 0
        app_id += 1
    
    connection.close()

    with open('review_data.json', 'w', encoding='utf-8') as out_file:
        json.dump(data, out_file, indent=4)


def isValidAppID(connection, ID):
    data = get_json(connection, '/appreviews/' + str(ID) + '?json=1')
    return data["success"] == 1

def request(connection, ID):
    j = get_json(connection, '/appreviews/' + str(ID) + '?json=1')
    print(json.dumps(j, indent=4))
    connection.close()
    
def get_json(connection, url):
    connection.request("GET", url)
    response = connection.getresponse()
    data = response.read()
    return json.loads(data.decode("utf-8"))
        
if __name__ == '__main__':
    main()
