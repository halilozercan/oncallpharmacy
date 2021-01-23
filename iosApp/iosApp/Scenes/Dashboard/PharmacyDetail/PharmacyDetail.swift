//
//  PharmacyDetail.swift
//  iosApp
//
//  Created by Çağatay Emekci on 21.12.2020.
//  Copyright © 2020 orgName. All rights reserved.
//

import SwiftUI
import shared
struct PharmacyDetail: View {
    var pharmacy: Pharmacy
    var body: some View {
        GeometryReader { geometry in
            VStack {
                ZStack {
                    Color.mainCellBackColor
                    VStack {
                        HStack {
                            Image.iconsPillBottle
                                .frame(width: 50, height: 50, alignment: /*@START_MENU_TOKEN@*/.center/*@END_MENU_TOKEN@*/)
                            Spacer()
                            VStack(alignment: .leading) {
                                Text("\(pharmacy.name)")
                                    .font(.headline)
                                    .foregroundColor(.gray)
                                Button(action: {
                                    guard let url = pharmacy.phone.phoneNumber() else { return }
                                    UIApplication.shared.open(url)
                                }) {
                                    Text("\(pharmacy.phone)")
                                        .font(.subheadline)
                                        .foregroundColor(.blue)
                                }
                                
                            }
                        }
                        .padding()
                        VStack(alignment: .leading) {
                            Text("\(pharmacy.address)")
                                .fixedSize(horizontal: false, vertical: true)
                                .font(.subheadline)
                                .foregroundColor(.gray)
                            Button(action: {
                                let url = URL(string: "maps://?q=Title&ll=\(pharmacy.latitude),\(pharmacy.longitude)")!
                                if UIApplication.shared.canOpenURL(url) {
                                    UIApplication.shared.open(url)
                                }
                            }) {
                                Text("Yol Tarifi Al")
                                    .foregroundColor(.blue)
                                    .padding([.top], 8)
                            }
                        }
                    }
                    .padding(8)
                }
                .cornerRadius(10.0)
                .rotation3DEffect(
                    Angle(degrees: Double(geometry.frame(in: .global).minX - 40) / -40),
                    axis: (x: 0, y: 10.0, z: 0)
                )
                .padding(8)
            }
        }.frame(width: 300, height: 200)
    }
}

extension String {
    
    func phoneNumber() -> URL? {
        let phone = self.filter { $0 != "-" && $0 != "(" && $0 != " " && $0 != ")" }
        let tel = "tel://"
        let formattedString = tel + phone
        guard let url = URL(string: formattedString) else {
            return nil
        }
        return url
    }
    
    func phoneNumberFormat() -> String {
        self.filter { $0 != "-" && $0 != "(" && $0 != " " && $0 != ")" }
    }
}
